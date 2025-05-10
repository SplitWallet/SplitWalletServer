package org.example.splitwalletserver.server.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDTO {
    @Schema(description = "Имя или электронная почта пользователя", example = "AlexM")
    private String login;

    @Schema(description = "Пароль пользователя", example = "12345")
    private String password;
}
