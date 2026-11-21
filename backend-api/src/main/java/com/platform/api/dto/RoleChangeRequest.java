package com.platform.api.dto;

import com.platform.api.entity.Role;
import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(
    @NotNull Role role
) {}