package com.platform.api.dto;

import java.util.List;

// Формат ошибки строго по ТЗ: {code, message, details}
public record ErrorResponse(
    String code,
    String message,
    List<String> details
) {}