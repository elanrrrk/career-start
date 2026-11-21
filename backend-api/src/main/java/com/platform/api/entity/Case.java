package com.platform.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cases")
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "case_tags", joinColumns = @JoinColumn(name = "case_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    private boolean isPublished = false;

    @Column(nullable = false)
    private int timeLimitMinutes = 60; // Лимит времени на прохождение

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Оптимистичная блокировка по ТЗ (код 409 при конкурентном изменении)
    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Case() {}

    public Case(String title, String description, String category, List<String> tags, int timeLimitMinutes, User author) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.tags = (tags != null) ? tags : new ArrayList<>();
        this.timeLimitMinutes = timeLimitMinutes;
        this.author = author;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }

    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(int timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}