package org.example.splitwalletserver.server.validation;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import org.example.splitwalletserver.server.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

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

	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ErrorResponse onEntityNotFoundException(EntityNotFoundException e) {
		return new ErrorResponse(404, e.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorResponse onIllegalArgumentException(IllegalArgumentException e) {
		return new ErrorResponse(400, e.getMessage());
	}


	@ExceptionHandler(IllegalStateException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ErrorResponse onIllegalStateException(IllegalStateException e) {
		return new ErrorResponse(400, e.getMessage());
	}

	@ExceptionHandler(ResponseStatusException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public ErrorResponse onResponseStatusException(ResponseStatusException e) {
		return new ErrorResponse(409, e.getReason());
	}

	@ExceptionHandler(NotAuthorizedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public ErrorResponse onNotAuthorizedException(NotAuthorizedException e) {
		return new ErrorResponse(409, e.getMessage());
	}

	@ExceptionHandler(ForbiddenException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ResponseBody
	public ErrorResponse onForbiddenException(ForbiddenException e) {
		return new ErrorResponse(403, e.getMessage());
	}
}