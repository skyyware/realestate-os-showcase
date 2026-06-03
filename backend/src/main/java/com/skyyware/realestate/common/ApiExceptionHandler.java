package com.skyyware.realestate.common;

import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ApiError("bad_request", exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request ist ungültig.");
        return ResponseEntity.badRequest().body(new ApiError("validation_error", message, Instant.now()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiError> handleState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError("unauthorized", exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> handleUnexpected(Exception exception) {
        log.error("Unhandled API exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "code", "internal_error",
                "message", "Unerwarteter Fehler.",
                "timestamp", Instant.now().toString()
        ));
    }

    record ApiError(String code, String message, Instant timestamp) {
    }
}
