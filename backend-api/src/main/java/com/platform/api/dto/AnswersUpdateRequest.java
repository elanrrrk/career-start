package com.platform.api.dto;

import java.util.List;

public record AnswersUpdateRequest(
        String answers,
        List<String> fileUrls
) {}