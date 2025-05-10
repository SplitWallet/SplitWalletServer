package com.example.notificationservice.db;

import com.example.notificationservice.other.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "user_fcm_tokens")
public class FcmToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fcm_token")
    private Set<String> tokens = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "user_entity_id", nullable = false)
    private User user;
}