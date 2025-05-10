package org.example.authservice.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.example.authservice.other.Group;

import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "user_entity")
public class User {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "EMAIL")
    private String email;

    @JsonIgnore
    @ManyToMany(mappedBy = "members")
    private List<Group> groups = new ArrayList<>();
}
