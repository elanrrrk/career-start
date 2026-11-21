package com.platform.api.exception;

import com.platform.api.dto.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Прямой перехват бизнес-ошибок (включая блокировку 403 и неверный пароль 401)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        String code = switch (ex.getStatusCode().value()) {
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            default -> "BAD_REQUEST";
        };

        String reason = ex.getReason() != null ? ex.getReason() : ex.getMessage();

        ErrorResponse error = new ErrorResponse(
            code,
            reason,
            List.of(reason)
        );
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // Обработка ошибок валидации полей JSON (@NotBlank, @Email и т.д.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .toList();

        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Ошибка валидации переданных данных",
            details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Оптимистичные блокировки (Optimistic Locking) по ТЗ -> статус 409 Conflict
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockException ex) {
        ErrorResponse error = new ErrorResponse(
            "CONCURRENT_MODIFICATION",
            "Запись была изменена другим процессом. Обновите данные.",
            List.of(ex.getMessage() != null ? ex.getMessage() : "Optimistic lock error")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Ошибки нехватки прав доступа -> статус 403 Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse(
            "FORBIDDEN",
            "У вашей роли недостаточно прав для выполнения этой операции",
            List.of(ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // Обработка непредвиденных системных сбоев -> статус 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "Внутренняя ошибка сервера",
            List.of(ex.getMessage() != null ? ex.getMessage() : "Unknown exception")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}