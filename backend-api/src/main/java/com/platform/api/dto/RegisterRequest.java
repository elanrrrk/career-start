package com.platform.api.dto;

import com.platform.api.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, message = "Пароль должен содержать минимум 6 символов") String password,
    Role role
) {}