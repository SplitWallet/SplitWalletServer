package org.example.groupsservice.other;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.groupsservice.db.Group;

import java.util.ArrayList;
import java.util.List;


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

    @JsonIgnore
    @ManyToMany(mappedBy = "members")
    private List<Group> groups = new ArrayList<>();

    public User(String id){
        this.id = id;
    }
}
