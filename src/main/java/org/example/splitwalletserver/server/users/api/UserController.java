package org.example.splitwalletserver.server.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.users.services.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.example.splitwalletserver.server.users.dto.LoginUserDTO;
import org.example.splitwalletserver.server.users.dto.UserDTO;
import org.example.splitwalletserver.server.users.services.UserService;

import java.util.Collections;

@RestController
@AllArgsConstructor
@Validated
@Tag(name = "User", description = "Operations about user")
public class UserController {
    private final UserService userService;
    private final UserServiceImpl keycloakUserService;

    @PostMapping("/registration")
    @Operation(summary = "Создать нового пользователя",
            description = "Регистрация нового пользователя в системе.")
    public ResponseEntity<Object> addUser(@RequestBody @Valid UserDTO userDTO) {

        keycloakUserService.createUser(userDTO);
        return new ResponseEntity<>(
                Collections.
                        singletonMap("jwtToken",keycloakUserService.login
                                (new LoginUserDTO(userDTO.getName(),userDTO.getPassword()))),
                HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Войти в систему",
            description = "Аутентификация пользователя с использованием логина и пароля.")
    public ResponseEntity<Object> loginUser(@RequestBody LoginUserDTO loginUserDTO) {
        return new ResponseEntity<>(
                Collections.
                        singletonMap("jwtToken",keycloakUserService.login(loginUserDTO)),
                HttpStatus.CREATED);
    }


    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable String userId) {
        keycloakUserService.deleteUserById(userId);
    }
}
