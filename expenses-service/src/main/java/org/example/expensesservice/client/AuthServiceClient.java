package org.example.expensesservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.example.expensesservice.other.Group;
import org.example.expensesservice.other.User;
import org.example.expensesservice.request.NotificationRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Group getGroupById(String authHeader, Long groupId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/groups-service/groups/" + groupId))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Group.class);
            } else {
                throw new EntityNotFoundException("Ошибка при вызове Auth Service: " + response.statusCode() +
                        " Body :" + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EntityNotFoundException("Вызов Auth Service прерван");

        } catch (IOException e) {
            throw new EntityNotFoundException("Ошибка сети: " + e.getMessage());
        }
    }

    public HttpResponse<String> sendNotification(String userId, NotificationRequest notificationRequest) {
        try {
            String url = "http://localhost:8080/notification-service/users/" + userId + "/notifications";

            String requestBody = objectMapper.writeValueAsString(notificationRequest);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));


            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response;

            } else if (response.statusCode() == 404){
                System.out.println(response.body());
                return response;
            } else{
                throw new EntityNotFoundException("Ошибка при вызове Notification Service: " + response.statusCode() +
                        " Body: " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EntityNotFoundException("Вызов Notification Service прерван");
        } catch (IOException e) {
            throw new EntityNotFoundException("Ошибка сети при вызове Notification Service: " + e.getMessage());
        }
    }

    public Map<String, HttpResponse<String>> sendNotificationToMultipleUsers(
            List<User> users,
            NotificationRequest notificationRequest) {

        Map<String, HttpResponse<String>> responses = new HashMap<>();

        for (User user : users) {
            HttpResponse<String> response = sendNotification(user.getId(), notificationRequest);
            responses.put(user.getId(), response);
        }

        return responses;
    }
}