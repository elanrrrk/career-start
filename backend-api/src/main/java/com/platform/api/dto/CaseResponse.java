package com.platform.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CaseResponse(
    UUID id,
    String title,
    String description,
    String category,
    List<String> tags,
    boolean isPublished,
    int timeLimitMinutes,
    UUID authorId,
    Long version,
    Instant createdAt,
    Instant updatedAt
) {}