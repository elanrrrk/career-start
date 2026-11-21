package com.platform.api.dto;

import jakarta.validation.constraints.Email;

public record UpdateProfileRequest(
        @Email(message = "Некорректный формат email")
        String email,
        String firstName,
        String lastName
) {}