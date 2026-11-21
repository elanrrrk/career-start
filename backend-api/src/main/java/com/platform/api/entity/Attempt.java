package com.platform.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attempts")
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case targetCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    // Хранение ответов в формате JSONB
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answers = "{}";

    // Ключ идемпотентности по ТЗ для исключения повторных списаний/отправок
    @Column(unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant deadline;

    private Instant submittedAt;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Attempt() {}

    public Attempt(Case targetCase, User user, Instant deadline) {
        this.targetCase = targetCase;
        this.user = user;
        this.deadline = deadline;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Case getTargetCase() { return targetCase; }
    public void setTargetCase(Case targetCase) { this.targetCase = targetCase; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }

    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Instant getDeadline() { return deadline; }
    public void setDeadline(Instant deadline) { this.deadline = deadline; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}