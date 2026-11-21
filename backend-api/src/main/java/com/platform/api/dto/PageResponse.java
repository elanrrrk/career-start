package com.platform.api.dto;

import java.util.List;

// Единый стандарт пагинации из ТЗ: {items, total, page, limit}
public record PageResponse<T>(
    List<T> items,
    long total,
    int page,
    int limit
) {}