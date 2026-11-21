package com.platform.api.dto;

import java.time.Instant;

public record PresignedUrlResponse(
    String uploadUrl,
    String fileKey,
    Instant expiresAt
) {}