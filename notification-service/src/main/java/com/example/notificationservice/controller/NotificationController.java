package com.example.notificationservice.controller;


import com.example.notificationservice.TestNotificationRequest.NotificationRequest;
import com.example.notificationservice.TestNotificationRequest.TokenRequest;
import com.example.notificationservice.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class NotificationController {
    private final MessageService messageService;

    @PostMapping("/{userId}/tokens")
    public ResponseEntity<?> addToken(
            @PathVariable String userId,
            @RequestBody TokenRequest token
    ) {
        System.out.println(token.getToken());
        messageService.saveToken(userId, token.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/notifications")
    public void sendNotification(
            @PathVariable String userId,
            @RequestBody NotificationRequest notificationRequest
    ) {
        messageService.sendNotificationToUser(userId, notificationRequest);
    }
}