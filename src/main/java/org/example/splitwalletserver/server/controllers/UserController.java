package org.example.splitwalletserver.server.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.ErrorResponse;
import org.example.splitwalletserver.server.dto.LoginUserDTO;
import org.example.splitwalletserver.server.dto.UserDTO;
import org.example.splitwalletserver.server.models.User;
import org.example.splitwalletserver.server.security.JWTUtil;
import org.example.splitwalletserver.server.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.Collections;

@RestController
@AllArgsConstructor
@Validated
@Tag(name = "User", description = "Operations about user")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final JWTUtil jwtUtil;

    @PostMapping("/registration")
    @Operation(summary = "Создать нового пользователя",
            description = "Регистрация нового пользователя в системе.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Неверный запрос",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"violations\": [{\"fieldName\": \"password\"," +
                                    " \"message\": \"Password should not be empty.\"}]}"))),
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"jwt-token\":" +
                                    " \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"))),
            @ApiResponse(responseCode = "409", description = "Email или имя уже зарегистрированы",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"httpStatus\": 409," +
                                    " \"message\": \"Email or Name already registered\"}"))),
            @ApiResponse(responseCode = "500", description = "Неожиданная ошибка при регистрации",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"httpStatus\": 500," +
                                    " \"message\": \"Unexpected error, please try again\"}")))
    })
    public ResponseEntity<Object> addUser(
            @RequestBody @Valid UserDTO userDTO
    ) {
        User user = convertMyUserDTOToMyUser(userDTO);
        String token = jwtUtil.generateToken(user.getName());

        return switch (userService.registration(user)) {
            case CONFLICT -> new ResponseEntity<>(new ErrorResponse(HttpStatus.CONFLICT.value(),
                    "Email or Name already registered"), HttpStatus.CONFLICT);
            case CREATED -> new ResponseEntity<>(Collections.singletonMap("jwtToken", token), HttpStatus.CREATED);
            default ->  new ResponseEntity<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error, please generate again"), HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    @PostMapping("/login")
    @Operation(summary = "Войти в систему",
            description = "Аутентификация пользователя с использованием логина и пароля.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Неверный запрос",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject())),
            @ApiResponse(responseCode = "200", description = "Успешный вход, возвращен токен",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"jwt-token\":" +
                                    " \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"))),
            @ApiResponse(responseCode = "404", description = "Неверный логин или пароль",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"httpStatus\": 404, " +
                                    "\"message\": \"Wrong login or password\"}"))),
            @ApiResponse(responseCode = "417", description = "Неожиданная ошибка при входе",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"httpStatus\": 417," +
                                    " \"message\": \"Unexpected error, please try again\"}"))),
            @ApiResponse(responseCode = "400",
                    content = @Content(examples = @ExampleObject()))
    })
    public ResponseEntity<Object> loginUser(@RequestBody LoginUserDTO loginUserDTO) {
        return switch (userService.login(loginUserDTO)) {
            case OK -> new ResponseEntity<>(Collections.singletonMap("jwtToken",
                    jwtUtil.generateToken(loginUserDTO.getLogin())),
                    HttpStatus.OK);
            case NOT_FOUND -> new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value()
                    , "Wrong login or password"), HttpStatus.NOT_FOUND);
            default -> new ResponseEntity<>(new ErrorResponse(HttpStatus.EXPECTATION_FAILED.value(),
                    "Unexpected error, please generate again"), HttpStatus.EXPECTATION_FAILED);
        };
    }


    private User convertMyUserDTOToMyUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

}
