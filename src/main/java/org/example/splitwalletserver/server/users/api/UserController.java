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

@RestController
@AllArgsConstructor
@Validated
@Tag(name = "User", description = "Operations about user")
public class UserController {
    private final UserServiceImpl userService;

    @PostMapping("/registration")
    @Operation(summary = "Создать нового пользователя",
            description = "Регистрация нового пользователя в системе.")
    public ResponseEntity<Object> addUser(@RequestBody @Valid UserDTO userDTO) {

        userService.createUser(userDTO);
        return new ResponseEntity<>(
                userService.login(new LoginUserDTO(userDTO.getName(),userDTO.getPassword())),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Войти в систему",
            description = "Аутентификация пользователя с использованием логина и пароля.")
    public ResponseEntity<Object> loginUser(@RequestBody LoginUserDTO loginUserDTO) {
        return new ResponseEntity<>(
                userService.login(loginUserDTO),
                HttpStatus.CREATED
        );
    }


    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable String userId) {
        userService.deleteUserById(userId);
    }
}
