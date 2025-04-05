package org.example.splitwalletserver.server.users.model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.splitwalletserver.server.groups.db.Group;

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

    @ManyToMany(mappedBy = "members")
    private List<Group> groups;
}
