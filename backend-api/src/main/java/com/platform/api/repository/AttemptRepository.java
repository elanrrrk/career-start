package com.platform.api.repository;

import com.platform.api.entity.Attempt;
import com.platform.api.entity.AttemptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, UUID> {
    Page<Attempt> findByUserId(UUID userId, Pageable pageable);
    Page<Attempt> findByStatus(AttemptStatus status, Pageable pageable);
    Optional<Attempt> findByIdempotencyKey(String idempotencyKey);
    Optional<Attempt> findByUserIdAndTargetCaseIdAndStatusIn(UUID userId, UUID caseId, List<AttemptStatus> statuses);
}