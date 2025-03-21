package org.example.splitwalletserver.server.repositories;

import org.example.splitwalletserver.server.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String username);
    Optional<User> findByEmail(String email);
}