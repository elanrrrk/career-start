package com.platform.api.event;

import java.util.UUID;

public record AuditEvent(
        UUID actorId,
        String action,
        String entityName,
        String entityId,
        String details
) {}