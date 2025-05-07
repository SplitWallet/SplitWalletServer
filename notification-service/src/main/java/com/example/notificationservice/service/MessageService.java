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

    public void saveToken(String userId, String token) {
        if (token.length() > 255){
            throw new IllegalArgumentException("Не тот token");
        }
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

    public void delToken(String userId, String token) {
        if (token.length() > 255){
            throw new IllegalArgumentException("Не тот token");
        }
        var fcmTokenOptional = fcmTokenRepository.findByUserId(userId);

        if (fcmTokenOptional.isEmpty()) {
            return;
        }
        var fcmToken = fcmTokenOptional.get();


        if (fcmToken.getTokens().remove(token)) {
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
