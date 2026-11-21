package com.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.platform.api.entity.AttemptStatus;

import java.time.Instant;
import java.util.UUID;

public record AttemptResponse(
        UUID id,
        UUID caseId,
        UUID userId,
        AttemptStatus status,
        String answers,
        Instant deadline,
        @JsonProperty("server_time")
        Instant serverTime,
        Instant submittedAt,
        Integer score,
        String feedback,
        Long version,
        Instant createdAt
) {}