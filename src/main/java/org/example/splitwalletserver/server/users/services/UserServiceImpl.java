package org.example.splitwalletserver.server.users.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.example.splitwalletserver.server.config.KeycloakAdminClientProperties;
import org.example.splitwalletserver.server.users.dto.LoginUserDTO;
import org.example.splitwalletserver.server.users.dto.UserDTO;
import org.example.splitwalletserver.server.users.model.User;
import org.example.splitwalletserver.server.users.repositories.UserRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${keycloak.realm}")
    private String realm;

    private Keycloak keycloak;

    private static KeycloakAdminClientProperties keycloakAdminClientProperties;
    private final UserRepository userRepository;

    static Logger logger = Logger.getLogger(String.valueOf(UserServiceImpl.class));

    public UserServiceImpl(Keycloak keycloak, UserRepository userRepository, KeycloakAdminClientProperties keycloakAdminClientProperties) {
        this.keycloak = keycloak;
        this.userRepository = userRepository;
        this.keycloakAdminClientProperties = keycloakAdminClientProperties;
    }

    @Override
    public UserDTO createUser(UserDTO user) {
        var userRepresentation = getUserRepresentation(user);
        var usersResource = getUsersResource();

        try(Response response = usersResource.create(userRepresentation)) {

            if (Objects.equals(201, response.getStatus())) {
                return user;
            }
            else if (Objects.equals(409, response.getStatus()))
                throw new ResponseStatusException(HttpStatus.CONFLICT,"This user already exists!");
        }
        throw new IllegalArgumentException("Unknown error");
    }

    private static UserRepresentation getUserRepresentation(UserDTO user) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setEmail(user.getEmail());
        newUser.setEmailVerified(true);
        newUser.setUsername(user.getName());
        newUser.setEnabled(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(user.getPassword());
        cred.setTemporary(false);

        newUser.setCredentials(List.of(cred));
        return newUser;
    }

    @Override
    public Optional<String> login(LoginUserDTO loginUserDTO) {
        HttpResponse<String> response;
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(keycloakAdminClientProperties.getUrl() +
                                    "/realms/" + keycloakAdminClientProperties.getRealm() + "/protocol/openid-connect/token"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    Map.of(
                                                    "client_id", keycloakAdminClientProperties.getClientId(),
                                                    "client_secret", keycloakAdminClientProperties.getSecret(),
                                                    "grant_type", "password",
                                                    "username", loginUserDTO.getLogin(),
                                                    "password", loginUserDTO.getPassword()
                                            )
                                            .entrySet().stream().map(entry ->
                                                    URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                                                            + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                                            .collect(Collectors.joining("&"))
                            ))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() == HttpStatus.OK.value()) {
                String res = new ObjectMapper().readTree(response.body()).get("access_token").asText();
                return Optional.ofNullable(res);
            }
            else
                throw new EntityNotFoundException("Wrong login or password");
        } catch (IOException e) {
            logger.warning(e.getMessage());
        } catch (InterruptedException e) {
            logger.warning(e.getMessage());
            Thread.currentThread().interrupt();
        }
        throw new IllegalArgumentException("Unexpected error, please generate again");
    }



    private UsersResource getUsersResource() {
        RealmResource realm1 = keycloak.realm(realm);
        return realm1.users();
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        return  getUsersResource().get(userId).toRepresentation();
    }

    @Override
    public void deleteUserById(String userId) {
        getUsersResource().delete(userId);
    }


    public User getCurrentUser() {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getToken().getClaimAsString("email");

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("User not found for the given username"));
    }
}
