package org.example.groupsservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.example.groupsservice.other.User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public User getCurrentUser(String authHeader) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/auth-service/currentUser"))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                // Десериализация JSON-ответа в объект User (например, через Jackson)
                return objectMapper.readValue(response.body(), User.class);
            } else {
                throw new EntityNotFoundException("Ошибка при вызове Auth Service: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EntityNotFoundException("Вызов Auth Service прерван");

        } catch (IOException e) {
            throw new EntityNotFoundException("Ошибка сети: " + e.getMessage());
        }
    }
}