package org.example.splitwalletserver.server.groups.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.splitwalletserver.server.expenses.db.Expense;
import org.example.splitwalletserver.server.users.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Название группы не может быть пустым")
    @Size(min = 3, max = 100, message = "Название группы должно быть от 3 до 100 символов")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_owner_id", nullable = false)
    private User userOwner;

    @Column(name = "unique_code", nullable = false, unique = true)  //todo предусмотреть уникальность повторной проверкой
    private String uniqueCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_entity_id")
    )
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> events = new ArrayList<>();

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;
}