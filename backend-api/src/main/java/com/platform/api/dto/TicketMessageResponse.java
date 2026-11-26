package com.platform.api.dto;

import com.platform.api.entity.MessageSenderRole;
import java.time.Instant;
import java.util.UUID;

public record TicketMessageResponse(
        UUID id,
        UUID senderId,
        MessageSenderRole senderRole,
        String message,
        Instant createdAt
) {}