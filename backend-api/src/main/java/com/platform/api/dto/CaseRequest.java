package com.platform.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CaseRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String category,
    List<String> tags,
    @Min(1) int timeLimitMinutes
) {}