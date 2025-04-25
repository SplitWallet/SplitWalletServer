package org.example.authservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.example.authservice.config.KeycloakAdminClientProperties;
import org.example.authservice.dto.GoogleToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class AuthServiceClient {
    private final HttpClient httpClient;

    private ObjectMapper objectMapper = new ObjectMapper();


    public AuthServiceClient() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public GoogleToken loginByGoogle(KeycloakAdminClientProperties keycloakAdminClientProperties, GoogleToken googleToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(keycloakAdminClientProperties.getUrl() +
                            "/realms/" + keycloakAdminClientProperties.getRealm() +
                            "/protocol/openid-connect/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:token-exchange", StandardCharsets.UTF_8) +
                                    "&client_id=" + URLEncoder.encode(keycloakAdminClientProperties.getClientId(), StandardCharsets.UTF_8) +
                                    "&client_secret=" + URLEncoder.encode(keycloakAdminClientProperties.getSecret(), StandardCharsets.UTF_8) +
                                    "&subject_token=" + URLEncoder.encode(googleToken.getAccess_token(), StandardCharsets.UTF_8) +
                                    "&subject_issuer=" + URLEncoder.encode("google", StandardCharsets.UTF_8)
                    ))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), GoogleToken.class);
            } else {
                throw new EntityNotFoundException("Ошибка Auth Service: " + response.statusCode() +
                        " Body :" + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EntityNotFoundException("Вызов Auth Service прерван");
        } catch (IOException e) {
            throw new EntityNotFoundException("Ошибка сети: " + e.getMessage());
        }
    }

}