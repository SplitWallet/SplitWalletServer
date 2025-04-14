package org.example.splitwalletserver.server.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
	@NotEmpty(message = "Name should not be empty.")
	@Size(min = 2, max = 30, message = "Expected size between 2 and 30 sym.")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Invalid format. Use a-Z or 0-9 symbols")
	@Schema(description = "Имя пользователя", example = "AlexM")
	private String name;

	@NotEmpty(message = "Email should not be empty.")
	@Email(message = "Wrong email format.")
	@Schema(description = "Электронная почта пользователя ", example = "alex@email.com")
	private String email;

	@NotEmpty(message = "Password should not be empty.")
	@Schema(description = "Пароль пользователя", example = "12345")
	private String password;
}
