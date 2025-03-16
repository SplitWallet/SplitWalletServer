package org.example.splitwalletserver.server.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.example.splitwalletserver.server.dto.LoginUserDTO;
import org.example.splitwalletserver.server.models.User;
import org.example.splitwalletserver.server.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Synchronized
    public HttpStatus registration(User user) {
        if (!(userRepository.findByName(user.getName()).isPresent()
                || userRepository.findByEmail(user.getEmail()).isPresent())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return HttpStatus.CREATED;
        } else {
            return HttpStatus.CONFLICT;
        }
    }

    @Synchronized
    public HttpStatus login(LoginUserDTO loginUserDTO) {
        HttpStatus status;
        try {
            User searchedUser = findByLogin(loginUserDTO.getLogin());
            if (passwordEncoder.matches(loginUserDTO.getPassword(), searchedUser.getPassword())) {
                status = HttpStatus.OK;
            } else {
                status = HttpStatus.NOT_FOUND;
            }
        } catch (NoSuchElementException | UsernameNotFoundException e) {
            status = HttpStatus.NOT_FOUND;
        }

        return status;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return findByLogin(userDetails.getUsername());
        }
        throw new UsernameNotFoundException("User not authenticated");
    }

    public User findByLogin(String username) {
        return userRepository
                .findByName(username)
                .orElseGet(
                        () -> userRepository.findByEmail(username)
                                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"))
                );
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(()->new EntityNotFoundException("User with id " + id + " not found"));
    }
}
