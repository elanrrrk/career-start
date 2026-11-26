package com.platform.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RateExpertRequest(
        @NotNull(message = "Оценка обязательна")
        @Min(value = 1, message = "Минимальная оценка: 1 звезда")
        @Max(value = 5, message = "Максимальная оценка: 5 звезд")
        Integer score,
        String comment
) {}