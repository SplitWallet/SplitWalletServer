package org.example.expensesuserservice.other;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@NoArgsConstructor
@Table(name = "user_entity")
public class User {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "EMAIL")
    private String email;

    public User(String userId) {
        this.id = userId;
    }

}
