package org.example.splitwalletserver.server.validation;

import jakarta.validation.ConstraintViolationException;
import org.example.splitwalletserver.server.controllers.UserController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice()
public class ErrorHandlingControllerAdvice {

	@ResponseBody
	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ValidationErrorResponse onConstraintValidationException(
					ConstraintViolationException e
	) {
		final List<Violation> violations = e.getConstraintViolations().stream()
						.map(
										violation -> new Violation(
														violation.getPropertyPath().toString(),
														violation.getMessage()
										)
						)
						.toList();
		return new ValidationErrorResponse(violations);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ValidationErrorResponse onMethodArgumentNotValidException(
					MethodArgumentNotValidException e
	) {
		final List<Violation> violations = e.getBindingResult().getFieldErrors().stream()
						.map(error -> new Violation(error.getField(), error.getDefaultMessage()))
						.toList();
		return new ValidationErrorResponse(violations);
	}

}