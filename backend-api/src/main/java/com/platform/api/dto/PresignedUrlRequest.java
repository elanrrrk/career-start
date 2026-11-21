package com.platform.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(
    @NotBlank String fileName,
    @NotBlank String mimeType,
    @Min(1) long fileSize
) {}