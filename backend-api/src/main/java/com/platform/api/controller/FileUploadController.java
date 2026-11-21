package com.platform.api.controller;

import com.platform.api.dto.PresignedUrlRequest;
import com.platform.api.dto.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Tag(name = "Files", description = "Загрузка файлов через Presigned URL с валидацией MIME и размера")
@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // Максимальный лимит: 20 МБ
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
        "application/pdf",
        "application/zip",
        "image/png",
        "image/jpeg",
        "text/plain"
    );

    @Operation(summary = "Получить Presigned URL для прямой загрузки файла")
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(@Valid @RequestBody PresignedUrlRequest req) {
        // Валидация размера по ТЗ
        if (req.fileSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Превышен максимальный лимит размера файла (20 МБ)");
        }

        // Валидация MIME-типа по ТЗ
        if (!ALLOWED_MIME_TYPES.contains(req.mimeType().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недопустимый MIME-тип файла: " + req.mimeType());
        }

        String fileKey = "uploads/" + UUID.randomUUID() + "-" + req.fileName();
        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

        // Генерация подписанной ссылки для прямой загрузки (S3-совместимый эндпоинт)
        String mockUploadUrl = "https://storage.platform.local/" + fileKey + "?signature=" + UUID.randomUUID();

        return ResponseEntity.ok(new PresignedUrlResponse(mockUploadUrl, fileKey, expiresAt));
    }
}