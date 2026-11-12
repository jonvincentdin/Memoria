package com.csit321g1.lukydizy.memoria.exception.handler;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.csit321g1.lukydizy.memoria.dto.ApiError;
import com.csit321g1.lukydizy.memoria.exception.EmailAlreadyUsedException;
import com.csit321g1.lukydizy.memoria.exception.InvalidCredentialsException;
import com.csit321g1.lukydizy.memoria.exception.InvalidRegistrationException;
import com.csit321g1.lukydizy.memoria.exception.UserNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    /** Returns a conflict response when an email is already in use. */
    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyUsed(EmailAlreadyUsedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(exception.getMessage()));
    }

    /** Returns an unauthorized response for invalid credentials. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(exception.getMessage()));
    }

    /** Returns a bad request response for invalid registration data. */
    @ExceptionHandler(InvalidRegistrationException.class)
    public ResponseEntity<ApiError> handleInvalidRegistration(InvalidRegistrationException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    /** Returns a not found response when a requested user does not exist. */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(exception.getMessage()));
    }

    /** Returns the first validation error in the request. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Optional<String> message = exception.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .findFirst();
        return ResponseEntity.badRequest().body(new ApiError(message.orElse("Invalid request.")));
    }

    /** Returns a bad request response when the body cannot be parsed. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody() {
        return ResponseEntity.badRequest().body(new ApiError("Invalid request body."));
    }
}
