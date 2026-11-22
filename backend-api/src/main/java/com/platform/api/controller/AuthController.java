package com.platform.api.controller;

import com.platform.api.dto.*;
import com.platform.api.entity.Role;
import com.platform.api.entity.User;
import com.platform.api.event.AuditEvent;
import com.platform.api.repository.UserRepository;
import com.platform.api.security.JwtService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Authentication", description = "Регистрация, вход, обновление токена и профиль")
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
        }

        Role assignedRole = (req.role() != null) ? req.role() : Role.CLIENT;
        User user = new User(req.email(), passwordEncoder.encode(req.password()), assignedRole);
        userRepository.save(user);

        // Аудит регистрации
        eventPublisher.publishEvent(new AuditEvent(
                user.getId(),
                "REGISTER",
                "User",
                user.getId().toString(),
                "Успешная регистрация аккаунта с ролью " + assignedRole.name()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(
            new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isBlocked(), user.getCreatedAt())
        );
    }

    @Operation(summary = "Авторизация и получение пары access/refresh токенов вместе с профилем пользователя")
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль"));

        if (user.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ваш аккаунт заблокирован администратором");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
        }

        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refresh = jwtService.generateRefreshToken(user.getId());

        // Аудит успешного входа
        eventPublisher.publishEvent(new AuditEvent(
                user.getId(),
                "LOGIN",
                "User",
                user.getId().toString(),
                "Успешная аутентификация в системе"
        ));

        AuthResponse response = AuthResponse.of(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            access,
            refresh
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Обновление access-токена по refresh-токену")
    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        if (!jwtService.isTokenValid(req.refreshToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Недействительный или просроченный refresh токен");
        }

        Claims claims = jwtService.extractClaims(req.refreshToken());
        UUID userId = UUID.fromString(claims.getSubject());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));

        if (user.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Аккаунт заблокирован");
        }

        String newAccess = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefresh = jwtService.generateRefreshToken(user.getId());

        AuthResponse response = AuthResponse.of(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            newAccess,
            newRefresh
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Выход из системы (Logout)")
    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof String principal) {
            try {
                UUID userId = UUID.fromString(principal);
                eventPublisher.publishEvent(new AuditEvent(
                        userId,
                        "LOGOUT",
                        "User",
                        userId.toString(),
                        "Пользователь вышел из системы"
                ));
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(Map.of("message", "Сессия успешно завершена"));
    }

    @Operation(summary = "Получить профиль текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        return ResponseEntity.ok(
            new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isBlocked(), user.getCreatedAt())
        );
    }

    @Operation(summary = "Частичное обновление профиля текущего пользователя (PATCH /me)")
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateProfileRequest req,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (req.email() != null && !req.email().isBlank() && !req.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(req.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
            }
            user.setEmail(req.email());
        }

        userRepository.save(user);

        // Аудит обновления профиля
        eventPublisher.publishEvent(new AuditEvent(
                userId,
                "UPDATE_PROFILE",
                "User",
                userId.toString(),
                "Пользователь обновил данные профиля"
        ));

        return ResponseEntity.ok(
                new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.isBlocked(), user.getCreatedAt())
        );
    }
}