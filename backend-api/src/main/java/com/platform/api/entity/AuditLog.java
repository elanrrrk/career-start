package com.platform.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID actorId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 100)
    private String entityName;

    @Column(nullable = false)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AuditLog() {}

    public AuditLog(UUID actorId, String action, String entityName, String entityId, String details) {
        this.actorId = actorId;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.details = details;
    }

    public UUID getId() { return id; }
    public UUID getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getEntityName() { return entityName; }
    public String getEntityId() { return entityId; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}