package com.example.notificationservice.service;

import com.example.notificationservice.TestNotificationRequest.NotificationRequest;
import com.example.notificationservice.db.FcmToken;
import com.example.notificationservice.db.FcmTokenRepository;
import com.example.notificationservice.other.User;
import com.google.firebase.messaging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service

public class MessageService {

    private final FcmTokenRepository fcmTokenRepository;

    private final FirebaseMessaging firebaseMessaging;

    @Autowired
    public MessageService(FcmTokenRepository fcmTokenRepository, FirebaseMessaging firebaseMessaging) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.firebaseMessaging = firebaseMessaging;
    }

    public String sendToToken(String token, String title, String body) throws Exception {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();
        return FirebaseMessaging.getInstance().send(message);
    }

    public void saveToken(String userId, String token) {
        var fcmTokenOptional = fcmTokenRepository.findByUserId(userId);
        FcmToken fcmToken;
        if (fcmTokenOptional.isEmpty()) {
            fcmToken = new FcmToken();
            fcmToken.setUser(new User(userId));
        }
        else {
            fcmToken = fcmTokenOptional.get();
        }

        if (fcmToken.getTokens().add(token)) {
            fcmTokenRepository.save(fcmToken);
        }
    }

    public void sendNotificationToUser(String userId, NotificationRequest request) {
        var fcmTokenOptional = fcmTokenRepository.findByUserId(userId);
        FcmToken fcmTokens;
        if (fcmTokenOptional.isEmpty() || fcmTokenOptional.get().getTokens().isEmpty()) {
            return;
        }
        else {
            fcmTokens = fcmTokenOptional.get();
        }

        for (String token : fcmTokens.getTokens()) {
            var message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getMessage())
                            .build())
                    .build();

            try {
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
            }
        }
    }
}
