package com.example.api.catalog.exception;

import com.example.dto.ApiError;
import com.example.dto.ApiErrorDetailsInner;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException ex) {
        ApiError error = new ApiError("VALIDATION_ERROR", "Petición inválida", UUID.randomUUID().toString());
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                error.addDetailsItem(new ApiErrorDetailsInner(fieldError.getField(), fieldError.getDefaultMessage())));
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleParamValidation(ConstraintViolationException ex) {
        ApiError error = new ApiError("VALIDATION_ERROR", "Petición inválida", UUID.randomUUID().toString());
        ex.getConstraintViolations().forEach(violation ->
                error.addDetailsItem(new ApiErrorDetailsInner(violation.getPropertyPath().toString(), violation.getMessage())));
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Cuerpo de la petición no válido");
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message) {
        ApiError error = new ApiError(code, message, UUID.randomUUID().toString());
        return ResponseEntity.status(status).body(error);
    }
}
