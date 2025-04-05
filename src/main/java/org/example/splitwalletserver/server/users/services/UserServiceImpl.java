package org.example.splitwalletserver.server.users.services;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import org.example.splitwalletserver.server.config.KeycloakAdminClientProperties;
import org.example.splitwalletserver.server.users.dto.LoginUserDTO;
import org.example.splitwalletserver.server.users.dto.UserDTO;
import org.example.splitwalletserver.server.users.model.User;
import org.example.splitwalletserver.server.users.repositories.UserRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.logging.Logger;

@Service
public class UserServiceImpl implements UserService {

    @Value("${keycloak.realm}")
    private String realm;

    private final Keycloak keycloak;

    private final KeycloakAdminClientProperties keycloakAdminClientProperties;
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
            else {
                logger.warning("/createUser undocumented response code: " + response.getStatus());
                throw new IllegalArgumentException("Unknown error");
            }
        }
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
    public AccessTokenResponse login(LoginUserDTO loginUserDTO) {
        try (
                Keycloak localKeycloak  = KeycloakBuilder.builder()
                        .serverUrl(keycloakAdminClientProperties.getUrl())
                        .realm(realm)
                        .grantType("password")
                        .clientId(keycloakAdminClientProperties.getClientId())
                        .clientSecret(keycloakAdminClientProperties.getSecret())
                        .username(loginUserDTO.getLogin())
                        .password(loginUserDTO.getPassword())
                        .build()
        ) {
            return localKeycloak.tokenManager().getAccessToken();
        }
        catch (NotAuthorizedException e){
            throw new NotAuthorizedException("Incorrect login or password");
        }catch (ClientErrorException e){
            logger.warning(e.getMessage());
            throw new IllegalArgumentException("Invalid client configuration. Please contact the developer!");
        }catch (ProcessingException e) {
            logger.warning(e.getMessage());
            throw new IllegalArgumentException("Network error occurred. Please contact the developer!");
        }
    }
        private UsersResource getUsersResource() {
        RealmResource realm1 = keycloak.realm(realm);
        return realm1.users();
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        return  getUsersResource().get(userId).toRepresentation();
    }

    //TODO обработать ошибки
    @Override
    public void deleteUserById(String userId) {
        try (Response delete = getUsersResource().delete(userId)){}
    }


    public User getCurrentUser() {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getToken().getClaimAsString("email");

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("User not found for the given username"));
    }
}
