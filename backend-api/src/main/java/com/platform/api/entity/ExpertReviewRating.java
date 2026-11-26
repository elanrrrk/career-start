package com.platform.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "expert_review_ratings")
public class ExpertReviewRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private ReviewTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private User expert;

    @Column(nullable = false)
    private int score; // от 1 до 5 звезд

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ExpertReviewRating() {}

    public ExpertReviewRating(ReviewTicket ticket, User student, User expert, int score, String comment) {
        this.ticket = ticket;
        this.student = student;
        this.expert = expert;
        this.score = score;
        this.comment = comment;
    }

    public UUID getId() { return id; }
    public ReviewTicket getTicket() { return ticket; }
    public User getStudent() { return student; }
    public User getExpert() { return expert; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}