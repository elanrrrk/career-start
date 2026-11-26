package com.platform.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "review_tickets")
public class ReviewTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false, unique = true)
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_expert_id")
    private User assignedExpert;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExpertTicketStatus status = ExpertTicketStatus.QUEUED;

    @Column(columnDefinition = "TEXT")
    private String expertFeedback;

    @Column(nullable = false)
    private int questionsCount = 0;

    @Column(nullable = false)
    private int answersCount = 0;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public ReviewTicket() {}

    public ReviewTicket(Attempt attempt) {
        this.attempt = attempt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Attempt getAttempt() { return attempt; }
    public void setAttempt(Attempt attempt) { this.attempt = attempt; }

    public User getAssignedExpert() { return assignedExpert; }
    public void setAssignedExpert(User assignedExpert) { this.assignedExpert = assignedExpert; }

    public ExpertTicketStatus getStatus() { return status; }
    public void setStatus(ExpertTicketStatus status) { this.status = status; }

    public String getExpertFeedback() { return expertFeedback; }
    public void setExpertFeedback(String expertFeedback) { this.expertFeedback = expertFeedback; }

    public int getQuestionsCount() { return questionsCount; }
    public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }

    public int getAnswersCount() { return answersCount; }
    public void setAnswersCount(int answersCount) { this.answersCount = answersCount; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}