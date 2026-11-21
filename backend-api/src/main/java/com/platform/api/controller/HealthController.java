package com.platform.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "Health Check", description = "Проверка доступности сервера")
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Operation(summary = "Проверить статус сервера и получить server_time в ISO 8601 UTC")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "server_time", Instant.now().toString()
        ));
    }
}