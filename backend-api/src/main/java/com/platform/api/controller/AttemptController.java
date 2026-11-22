package com.platform.api.controller;

import com.platform.api.dto.AnswersUpdateRequest;
import com.platform.api.dto.AttemptResponse;
import com.platform.api.dto.PageResponse;
import com.platform.api.dto.ReviewRequest;
import com.platform.api.entity.Attempt;
import com.platform.api.entity.AttemptStatus;
import com.platform.api.entity.Case;
import com.platform.api.entity.User;
import com.platform.api.event.AuditEvent;
import com.platform.api.repository.AttemptRepository;
import com.platform.api.repository.CaseRepository;
import com.platform.api.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Attempts & Submissions", description = "Прохождение кейсов клиентом и проверка решений экспертом")
@RestController
@RequestMapping("/api/v1")
public class AttemptController {

    private final AttemptRepository attemptRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AttemptController(
            AttemptRepository attemptRepository,
            CaseRepository caseRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.attemptRepository = attemptRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Operation(summary = "Начать прохождение кейса (POST /cases/{id}/attempts)")
    @PostMapping("/cases/{caseId}/attempts")
    public ResponseEntity<AttemptResponse> startAttempt(@PathVariable UUID caseId, Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));

        Case targetCase = caseRepository.findById(caseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Кейс не найден"));

        if (!targetCase.isPublished()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя начать неопубликованный кейс");
        }

        Optional<Attempt> activeAttempt = attemptRepository.findByUserIdAndTargetCaseIdAndStatus(userId, caseId, AttemptStatus.IN_PROGRESS);
        if (activeAttempt.isPresent()) {
            Attempt existing = activeAttempt.get();
            if (Instant.now().isBefore(existing.getDeadline())) {
                return ResponseEntity.ok(mapToResponse(existing));
            }
        }

        Instant deadline = Instant.now().plus(targetCase.getTimeLimitMinutes(), ChronoUnit.MINUTES);
        Attempt attempt = new Attempt(targetCase, user, deadline);
        attemptRepository.save(attempt);

        // Аудит начала прохождения кейса
        eventPublisher.publishEvent(new AuditEvent(
                userId,
                "START_ATTEMPT",
                "Attempt",
                attempt.getId().toString(),
                "Начато решение кейса: " + targetCase.getTitle() + ", дедлайн до: " + deadline
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(attempt));
    }

    @Operation(summary = "Автосейв промежуточных ответов (PUT /attempts/{id}/answers)")
    @PutMapping("/attempts/{id}/answers")
    public ResponseEntity<AttemptResponse> autosaveAnswers(
        @PathVariable UUID id,
        @Valid @RequestBody AnswersUpdateRequest req,
        Authentication authentication
    ) {
        Attempt attempt = getAttemptAndVerifyOwner(id, authentication);

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Попытка уже сдана или проверена");
        }

        if (Instant.now().isAfter(attempt.getDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Время на решение кейса истекло");
        }

        attempt.setAnswers(req.answers());
        attemptRepository.save(attempt);

        return ResponseEntity.ok(mapToResponse(attempt));
    }

    @Operation(summary = "Финальная сдача кейса с ключом идемпотентности (POST /attempts/{id}/submit)")
    @PostMapping("/attempts/{id}/submit")
    public ResponseEntity<AttemptResponse> submitAttempt(
        @PathVariable UUID id,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        Authentication authentication
    ) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Attempt> existing = attemptRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                Attempt prev = existing.get();
                if (prev.getId().equals(id)) {
                    return ResponseEntity.ok(mapToResponse(prev));
                }
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency-Key уже использован для другой попытки");
            }
        }

        Attempt attempt = getAttemptAndVerifyOwner(id, authentication);

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Попытка уже была сдана ранее");
        }

        Instant now = Instant.now();
        if (now.isAfter(attempt.getDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дедлайн просрочен. Сдача отклонена сервером.");
        }

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(now);
        attempt.setIdempotencyKey(idempotencyKey);
        attemptRepository.save(attempt);

        // Аудит сдачи работы
        eventPublisher.publishEvent(new AuditEvent(
                attempt.getUser().getId(),
                "SUBMIT_ATTEMPT",
                "Attempt",
                attempt.getId().toString(),
                "Работа по кейсу \"" + attempt.getTargetCase().getTitle() + "\" успешно сдана"
        ));

        return ResponseEntity.ok(mapToResponse(attempt));
    }

    @Operation(summary = "Получить список своих попыток (GET /attempts)")
    @GetMapping("/attempts")
    public ResponseEntity<PageResponse<AttemptResponse>> getMyAttempts(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        Authentication authentication
    ) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, limit, Sort.by("createdAt").descending());

        Page<Attempt> attemptsPage = attemptRepository.findByUserId(userId, pageable);
        var items = attemptsPage.getContent().stream().map(this::mapToResponse).toList();

        return ResponseEntity.ok(new PageResponse<>(
            items,
            attemptsPage.getTotalElements(),
            page,
            limit
        ));
    }

    @Operation(summary = "Получить работы на проверку (GET /submissions?status=pending) - только EXPERT/ADMIN")
    @GetMapping("/submissions")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<PageResponse<AttemptResponse>> getSubmissions(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "pending") String status
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, limit, Sort.by("submittedAt").ascending());

        AttemptStatus queryStatus = "pending".equalsIgnoreCase(status) ? AttemptStatus.SUBMITTED : AttemptStatus.REVIEWED;
        Page<Attempt> submissionsPage = attemptRepository.findByStatus(queryStatus, pageable);
        var items = submissionsPage.getContent().stream().map(this::mapToResponse).toList();

        return ResponseEntity.ok(new PageResponse<>(
            items,
            submissionsPage.getTotalElements(),
            page,
            limit
        ));
    }

    @Operation(summary = "Оценить решение студента (POST /submissions/{id}/review) - только EXPERT/ADMIN")
    @PostMapping("/submissions/{id}/review")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<AttemptResponse> reviewSubmission(
        @PathVariable UUID id,
        @Valid @RequestBody ReviewRequest req,
        Authentication authentication
    ) {
        UUID reviewerId = UUID.fromString((String) authentication.getPrincipal());
        Attempt attempt = attemptRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Попытка не найдена"));

        if (attempt.getStatus() != AttemptStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Проверить можно только сданную работу (SUBMITTED)");
        }

        attempt.setStatus(AttemptStatus.REVIEWED);
        attempt.setScore(req.score());
        attempt.setFeedback(req.feedback());
        attemptRepository.save(attempt);

        // Аудит проверки работы экспертом
        eventPublisher.publishEvent(new AuditEvent(
                reviewerId,
                "REVIEW_SUBMISSION",
                "Attempt",
                attempt.getId().toString(),
                "Выставлена оценка: " + req.score() + " баллов за решение кейса " + attempt.getTargetCase().getTitle()
        ));

        return ResponseEntity.ok(mapToResponse(attempt));
    }

    private Attempt getAttemptAndVerifyOwner(UUID attemptId, Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        Attempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Попытка не найдена"));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не являетесь владельцем этой попытки");
        }
        return attempt;
    }

    private AttemptResponse mapToResponse(Attempt a) {
        return new AttemptResponse(
            a.getId(),
            a.getTargetCase().getId(),
            a.getUser().getId(),
            a.getStatus(),
            a.getAnswers(),
            a.getDeadline(),
            Instant.now(),
            a.getSubmittedAt(),
            a.getScore(),
            a.getFeedback(),
            a.getVersion(),
            a.getCreatedAt()
        );
    }
}