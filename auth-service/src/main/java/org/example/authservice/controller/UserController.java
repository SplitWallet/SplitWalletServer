package org.example.authservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import org.example.authservice.db.User;
import org.example.authservice.dto.GoogleToken;
import org.example.authservice.dto.LoginUserDTO;
import org.example.authservice.dto.UserDTO;
import org.example.authservice.service.UserServiceImpl;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;


@RestController
@AllArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @Hidden
    @GetMapping("/currentUser")
    public User getCurrentUser() {
        var user = userService.getCurrentUser();
        Hibernate.initialize(user.getGroups());
        return user;
    }

    @PostMapping("/registration")
    public ResponseEntity<Object> addUser(@RequestBody UserDTO userDTO) {

        userService.createUser(userDTO);
        return new ResponseEntity<>(
                Collections.
                        singletonMap("jwtToken",userService.login
                                (new LoginUserDTO(userDTO.getName(),userDTO.getPassword())).getToken()),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<Object> loginUser(@RequestBody LoginUserDTO loginUserDTO) {
        return new ResponseEntity<>(
                Collections.
                        singletonMap("jwtToken",userService.login
                                (new LoginUserDTO(loginUserDTO.getLogin(),loginUserDTO.getPassword())).getToken()),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login/google")
    public ResponseEntity<Object> loginUserByGoogle(@RequestBody GoogleToken googleToken) {
        return new ResponseEntity<>(
                Collections.
                        singletonMap("jwtToken",userService.loginByGoogle(googleToken).getAccess_token()),
                HttpStatus.CREATED
        );
    }
}
