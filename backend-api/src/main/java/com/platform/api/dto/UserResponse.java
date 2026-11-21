package com.platform.api.dto;

import com.platform.api.entity.Role;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    Role role,
    boolean isBlocked,
    Instant createdAt
) {}