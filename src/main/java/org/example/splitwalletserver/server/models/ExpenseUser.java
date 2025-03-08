package org.example.splitwalletserver.server.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "expense_users")
public class ExpenseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Уникальный идентификатор связи

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Пользователь, который должен оплатить часть расхода

    @ManyToOne
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense; // Расход, к которому относится эта связь

    @Column(nullable = false)
    private BigDecimal amount; // Сколько монет должен пользователь
}