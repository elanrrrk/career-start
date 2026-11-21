package com.platform.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Оценка эксперта с баллом от 0 до 100 и отзывом
public record ReviewRequest(
    @NotNull @Min(0) @Max(100) Integer score,
    String feedback
) {}