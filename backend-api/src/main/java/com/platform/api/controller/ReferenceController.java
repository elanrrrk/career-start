package com.platform.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.Duration;
import java.util.List;

@Tag(name = "References", description = "Справочники категорий и тегов с поддержкой ETag и кэширования")
@RestController
@RequestMapping("/api/v1/references")
public class ReferenceController {

    private static final List<String> CATEGORIES = List.of(
        "Архитектура", "Бэкенд-разработка", "Базы данных", "DevOps", "Безопасность"
    );

    private static final List<String> TAGS = List.of(
        "PostgreSQL", "Java", "Spring Boot", "HighLoad", "Docker", "REST API", "JWT"
    );

    @Operation(summary = "Получить справочник категорий с поддержкой ETag и 304 Not Modified")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(WebRequest request) {
        String eTag = "\"" + Integer.toHexString(CATEGORIES.hashCode()) + "\"";

        // Проверка совпадения If-None-Match: возвращает 304 Not Modified автоматически
        if (request.checkNotModified(eTag)) {
            return null;
        }

        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
            .eTag(eTag)
            .body(CATEGORIES);
    }

    @Operation(summary = "Получить справочник тегов с поддержкой ETag и 304 Not Modified")
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getTags(WebRequest request) {
        String eTag = "\"" + Integer.toHexString(TAGS.hashCode()) + "\"";

        if (request.checkNotModified(eTag)) {
            return null;
        }

        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
            .eTag(eTag)
            .body(TAGS);
    }
}