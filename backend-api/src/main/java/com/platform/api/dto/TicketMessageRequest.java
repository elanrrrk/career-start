package com.platform.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketMessageRequest(
        @NotBlank(message = "Сообщение не может быть пустым")
        String message
) {}