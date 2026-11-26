package com.platform.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ExpertReviewRatingResponse(
        UUID id,
        UUID ticketId,
        UUID expertId,
        int score,
        String comment,
        Instant createdAt
) {}