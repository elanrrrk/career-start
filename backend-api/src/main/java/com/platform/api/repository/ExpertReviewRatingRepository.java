package com.platform.api.repository;

import com.platform.api.entity.ExpertReviewRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpertReviewRatingRepository extends JpaRepository<ExpertReviewRating, UUID> {
    boolean existsByTicketId(UUID ticketId);
    Optional<ExpertReviewRating> findByTicketId(UUID ticketId);
}