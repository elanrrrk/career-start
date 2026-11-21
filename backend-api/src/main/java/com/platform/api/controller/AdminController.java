package com.platform.api.controller;

import com.platform.api.dto.BlockUserRequest;
import com.platform.api.dto.PageResponse;
import com.platform.api.dto.RoleChangeRequest;
import com.platform.api.dto.UserResponse;
import com.platform.api.entity.AuditLog;
import com.platform.api.entity.User;
import com.platform.api.event.AuditEvent;
import com.platform.api.repository.AuditLogRepository;
import com.platform.api.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Tag(name = "Admin", description = "Управление пользователями, блокировки и аудит (только ADMIN)")
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminController(
            UserRepository userRepository,
            AuditLogRepository auditLogRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Operation(summary = "Получить список пользователей (GET /admin/users)")
    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, limit, Sort.by("createdAt").descending());

        Page<User> usersPage = userRepository.findAll(pageable);
        var items = usersPage.getContent().stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.isBlocked(), u.getCreatedAt()))
                .toList();

        return ResponseEntity.ok(new PageResponse<>(items, usersPage.getTotalElements(), page, limit));
    }

    @Operation(summary = "Сменить роль пользователя (PATCH /admin/users/{id}/role)")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleChangeRequest req,
            Authentication authentication
    ) {
        UUID actorId = UUID.fromString((String) authentication.getPrincipal());
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        String oldRole = user.getRole().name();
        user.setRole(req.role());
        userRepository.save(user);

        // Асинхронная фиксация события в базу данных
        eventPublisher.publishEvent(new AuditEvent(
                actorId,
                "CHANGE_ROLE",
                "User",
                user.getId().toString(),
                "Роль изменена с " + oldRole + " на " + req.role()
        ));

        return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isBlocked(), user.getCreatedAt()));
    }

    @Operation(summary = "Заблокировать/разблокировать пользователя (PATCH /admin/users/{id}/block)")
    @PatchMapping("/users/{id}/block")
    public ResponseEntity<UserResponse> toggleUserBlock(
            @PathVariable UUID id,
            @Valid @RequestBody BlockUserRequest req,
            Authentication authentication
    ) {
        UUID actorId = UUID.fromString((String) authentication.getPrincipal());
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        user.setBlocked(req.blocked());
        userRepository.save(user);

        // Асинхронная фиксация события в базу данных
        eventPublisher.publishEvent(new AuditEvent(
                actorId,
                req.blocked() ? "BLOCK_USER" : "UNBLOCK_USER",
                "User",
                user.getId().toString(),
                "Статус блокировки: " + req.blocked()
        ));

        return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isBlocked(), user.getCreatedAt()));
    }

    @Operation(summary = "Просмотреть системный журнал аудита (GET /admin/audit-logs)")
    @GetMapping("/audit-logs")
    public ResponseEntity<PageResponse<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, limit);

        Page<AuditLog> logPage = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return ResponseEntity.ok(new PageResponse<>(logPage.getContent(), logPage.getTotalElements(), page, limit));
    }
}