package org.example.splitwalletserver.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true)
    @NotEmpty(message = "Name should not be empty.")
    @Size(min = 2, max = 30, message = "Expected size between 2 and 30 sym.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Invalid format. Use a-Z or 0-9 symbols")
    private String name;

    @Column(unique = true)
    @NotEmpty(message = "Email should not be empty.")
    @Email(message = "Wrong email format.")
    private String email;

    @Column(unique = true)
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format. Use + and digits (10-15 symbols).")
    private String phoneNumber;

    @NotEmpty(message = "Password should not be empty.")
    private String password;

    @ManyToMany(mappedBy = "members")
    private List<Group> groups;
}
