package org.example.expensesuserservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.example.expensesuserservice.other.Group;
import org.example.expensesuserservice.other.User;
import org.example.expensesuserservice.request.NotificationRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MultiServiceClient {

    private ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate;

    public MultiServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public User getCurrentUser() {
        try {
            ResponseEntity<User> response = restTemplate.exchange(
                    "http://auth-service/currentUser",
                    HttpMethod.GET,
                    null,
                    User.class
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Пользователь не найден");
        } catch (Exception e) {
            throw new EntityNotFoundException("Ошибка при вызове Auth Service: " + e.getMessage());
        }
    }

    public Group getGroupById(Long groupId) {
        try {
            ResponseEntity<Group> response = restTemplate.exchange(
                    "http://groups-service/groups/" + groupId,
                    HttpMethod.GET,
                    null,
                    Group.class
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Группа не найдена");
        } catch (Exception e) {
            throw new EntityNotFoundException("Ошибка при вызове Groups Service: " + e.getMessage());
        }
    }

    public ResponseEntity<String> sendNotification(String userId, NotificationRequest notificationRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NotificationRequest> entity = new HttpEntity<>(notificationRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "http://notification-service/users/" + userId + "/notifications",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response;
        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("Notification service response: " + e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new EntityNotFoundException("Ошибка при вызове Notification Service: " + e.getMessage());
        }
    }

    public Map<String, ResponseEntity<String>> sendNotificationToMultipleUsers(
            List<User> users,
            NotificationRequest notificationRequest) {

        Map<String, ResponseEntity<String>> responses = new HashMap<>();

        for (User user : users) {
            ResponseEntity<String> response = sendNotification(user.getId(), notificationRequest);
            responses.put(user.getId(), response);
        }

        return responses;
    }
    
}