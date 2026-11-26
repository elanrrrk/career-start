package com.platform.api.dto;

import com.platform.api.entity.ReviewMode;
import jakarta.validation.constraints.NotNull;

public record SubmitAttemptRequest(
        @NotNull(message = "Необходимо выбрать тип проверки (AI_ONLY, EXPERT_ONLY, AI_AND_EXPERT)")
        ReviewMode reviewMode
) {}