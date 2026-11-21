package com.platform.api.dto;

import jakarta.validation.constraints.NotNull;

// Автосейв ответов клиента
public record AnswersUpdateRequest(
    @NotNull String answers
) {}