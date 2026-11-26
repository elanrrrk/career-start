package com.platform.api.repository;

import com.platform.api.entity.ExpertTicketStatus;
import com.platform.api.entity.ReviewTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewTicketRepository extends JpaRepository<ReviewTicket, UUID> {

    @EntityGraph(attributePaths = {"attempt", "attempt.targetCase", "attempt.user", "assignedExpert"})
    Optional<ReviewTicket> findByAttemptId(UUID attemptId);

    @EntityGraph(attributePaths = {"attempt", "attempt.targetCase", "attempt.user", "assignedExpert"})
    Page<ReviewTicket> findByStatus(ExpertTicketStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"attempt", "attempt.targetCase", "attempt.user", "assignedExpert"})
    Page<ReviewTicket> findByAssignedExpertId(UUID expertId, Pageable pageable);

    @EntityGraph(attributePaths = {"attempt", "attempt.targetCase", "attempt.user", "assignedExpert"})
    Page<ReviewTicket> findByAssignedExpertIdAndStatus(UUID expertId, ExpertTicketStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"attempt", "attempt.targetCase", "attempt.user", "assignedExpert"})
    Optional<ReviewTicket> findById(UUID id);
}