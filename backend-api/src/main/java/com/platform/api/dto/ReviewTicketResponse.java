package com.platform.api.dto;

import com.platform.api.entity.ExpertTicketStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewTicketResponse(
        UUID id,
        UUID attemptId,
        UUID caseId,
        String caseTitle,
        UUID studentId,
        UUID assignedExpertId,
        ExpertTicketStatus status,
        String expertFeedback,
        int questionsCount,
        int answersCount,
        List<TicketMessageResponse> messages,
        Instant createdAt,
        Instant updatedAt
) {}