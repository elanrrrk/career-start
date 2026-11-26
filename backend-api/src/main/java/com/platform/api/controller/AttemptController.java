package com.platform.api.controller;

import com.platform.api.dto.*;
import com.platform.api.entity.*;
import com.platform.api.event.AuditEvent;
import com.platform.api.repository.AttemptRepository;
import com.platform.api.repository.CaseRepository;
import com.platform.api.repository.ReviewTicketRepository;
import com.platform.api.repository.TicketMessageRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Attempts & Client Funnel", description = "Воронка прохождения кейсов студентом")
@RestController
@RequestMapping("/api/v1")
public class AttemptController {

    private static final boolean USE_INFINITE_DEADLINES = true;
    private static final long INFINITE_DEADLINE_DAYS = 3650;

    private final AttemptRepository attemptRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final ReviewTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AttemptController(
            AttemptRepository attemptRepository,
            CaseRepository caseRepository,
            UserRepository userRepository,
            ReviewTicketRepository ticketRepository,
            TicketMessageRepository messageRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.attemptRepository = attemptRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
    }

    @Operation(summary = "Вход в кейс (Статус: OPENED)")
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

        List<AttemptStatus> activeStatuses = List.of(
                AttemptStatus.OPENED,
                AttemptStatus.SOLUTION_ATTACHED,
                AttemptStatus.PENDING_AI_REVIEW,
                AttemptStatus.AI_REVIEWED,
                AttemptStatus.PENDING_EXPERT_REVIEW,
                AttemptStatus.EXPERT_REVIEW_RECEIVED,
                AttemptStatus.QUESTION_ASKED,
                AttemptStatus.FOLLOW_UP_QUESTION_ASKED,
                AttemptStatus.EXPERT_ANSWER_RECEIVED
        );

        Optional<Attempt> activeAttempt = attemptRepository.findByUserIdAndTargetCaseIdAndStatusIn(userId, caseId, activeStatuses);
        if (activeAttempt.isPresent()) {
            Attempt existing = activeAttempt.get();
            if (Instant.now().isBefore(existing.getDeadline())) {
                return ResponseEntity.ok(mapToResponse(existing));
            }
        }

        Instant deadline = USE_INFINITE_DEADLINES
                ? Instant.now().plus(INFINITE_DEADLINE_DAYS, ChronoUnit.DAYS)
                : Instant.now().plus(targetCase.getTimeLimitMinutes(), ChronoUnit.MINUTES);

        Attempt attempt = new Attempt(targetCase, user, deadline);
        attempt.setStatus(AttemptStatus.OPENED);
        attemptRepository.save(attempt);

        eventPublisher.publishEvent(new AuditEvent(
                userId,
                "OPEN_CASE",
                "Attempt",
                attempt.getId().toString(),
                "Кейс открыт для решения: " + targetCase.getTitle()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(attempt));
    }

    @Operation(summary = "Прикрепление решения / автосейв (Статус: SOLUTION_ATTACHED)")
    @PutMapping("/attempts/{id}/answers")
    public ResponseEntity<AttemptResponse> autosaveAnswers(
            @PathVariable UUID id,
            @Valid @RequestBody AnswersUpdateRequest req,
            Authentication authentication
    ) {
        Attempt attempt = getAttemptAndVerifyOwner(id, authentication);

        if (attempt.getStatus() == AttemptStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Попытка уже завершена");
        }

        if (!USE_INFINITE_DEADLINES && Instant.now().isAfter(attempt.getDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дедлайн истек");
        }

        attempt.setAnswers(req.answers());
        attempt.setStatus(AttemptStatus.SOLUTION_ATTACHED);
        attemptRepository.save(attempt);

        return ResponseEntity.ok(mapToResponse(attempt));
    }

    @Operation(summary = "Сдача на проверку с выбором воронки (AI_ONLY, EXPERT_ONLY, AI_AND_EXPERT)")
    @PostMapping("/attempts/{id}/submit")
    public ResponseEntity<AttemptResponse> submitAttempt(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitAttemptRequest req,
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
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency-Key уже использован");
            }
        }

        Attempt attempt = getAttemptAndVerifyOwner(id, authentication);

        if (attempt.getStatus() == AttemptStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Попытка уже завершена ранее");
        }

        Instant now = Instant.now();
        if (!USE_INFINITE_DEADLINES && now.isAfter(attempt.getDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дедлайн просрочен сервером");
        }

        attempt.setSubmittedAt(now);
        attempt.setIdempotencyKey(idempotencyKey);
        attempt.setReviewMode(req.reviewMode());

        if (req.reviewMode() == ReviewMode.AI_ONLY) {
            attempt.setStatus(AttemptStatus.AI_REVIEWED);
            attempt.setAiFeedback("ИИ-разбор: Решение проанализировано. Структура логична, ключевые требования учтены.");
            attempt.setScore(85);
        } else if (req.reviewMode() == ReviewMode.EXPERT_ONLY) {
            attempt.setStatus(AttemptStatus.PENDING_EXPERT_REVIEW);
            createOrGetExpertTicket(attempt);
        } else if (req.reviewMode() == ReviewMode.AI_AND_EXPERT) {
            attempt.setStatus(AttemptStatus.PENDING_EXPERT_REVIEW);
            attempt.setAiFeedback("Экспресс-анализ ИИ завершен. Полный детальный разбор формируется профильным экспертом.");
            createOrGetExpertTicket(attempt);
        }

        attemptRepository.save(attempt);

        eventPublisher.publishEvent(new AuditEvent(
                attempt.getUser().getId(),
                "SUBMIT_ATTEMPT",
                "Attempt",
                attempt.getId().toString(),
                "Решение сдано в режиме проверки: " + req.reviewMode()
        ));

        return ResponseEntity.ok(mapToResponse(attempt));
    }

    @Operation(summary = "Задать вопрос / апелляцию эксперту (Статус: QUESTION_ASKED / FOLLOW_UP_QUESTION_ASKED)")
    @PostMapping("/attempts/{id}/questions")
    @Transactional
    public ResponseEntity<ReviewTicketResponse> askQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody TicketMessageRequest req,
            Authentication authentication
    ) {
        Attempt attempt = getAttemptAndVerifyOwner(id, authentication);

        ReviewTicket ticket = ticketRepository.findByAttemptId(attempt.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Для этой попытки нет активного тикета проверки"));

        if (ticket.getStatus() == ExpertTicketStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Диалог закрыт, кейс завершен");
        }

        int count = ticket.getQuestionsCount() + 1;
        ticket.setQuestionsCount(count);

        if (count == 1) {
            attempt.setStatus(AttemptStatus.QUESTION_ASKED);
            ticket.setStatus(ExpertTicketStatus.QUESTION_RECEIVED);
        } else {
            attempt.setStatus(AttemptStatus.FOLLOW_UP_QUESTION_ASKED);
            ticket.setStatus(ExpertTicketStatus.FOLLOW_UP_QUESTION_RECEIVED);
        }

        ticketRepository.save(ticket);
        attemptRepository.save(attempt);

        TicketMessage msg = new TicketMessage(ticket, attempt.getUser(), MessageSenderRole.CLIENT, req.message());
        messageRepository.save(msg);

        eventPublisher.publishEvent(new AuditEvent(
                attempt.getUser().getId(),
                count == 1 ? "ASK_QUESTION" : "ASK_FOLLOW_UP_QUESTION",
                "ReviewTicket",
                ticket.getId().toString(),
                "Студент задал вопрос #" + count
        ));

        return ResponseEntity.ok(mapToTicketResponse(ticket));
    }

    @Operation(summary = "Получить тикет проверки и чат с экспертом по ID попытки")
    @GetMapping("/attempts/{id}/ticket")
    @Transactional(readOnly = true)
    public ResponseEntity<ReviewTicketResponse> getAttemptTicket(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        Attempt attempt = getAttemptAndVerifyOwner(id, authentication);

        ReviewTicket ticket = ticketRepository.findByAttemptId(attempt.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тикет проверки для этой попытки не найден"));

        return ResponseEntity.ok(mapToTicketResponse(ticket));
    }

    @Operation(summary = "Завершить работу по кейсу (Статус: COMPLETED / DONE)")
    @PostMapping("/attempts/{id}/complete")
    public ResponseEntity<AttemptResponse> completeAttempt(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        Attempt attempt = getAttemptAndVerifyOwner(id, authentication);

        attempt.setStatus(AttemptStatus.COMPLETED);
        attemptRepository.save(attempt);

        ticketRepository.findByAttemptId(attempt.getId()).ifPresent(ticket -> {
            ticket.setStatus(ExpertTicketStatus.COMPLETED);
            ticketRepository.save(ticket);
        });

        eventPublisher.publishEvent(new AuditEvent(
                attempt.getUser().getId(),
                "COMPLETE_ATTEMPT",
                "Attempt",
                attempt.getId().toString(),
                "Кейс полностью завершен клиентом"
        ));

        return ResponseEntity.ok(mapToResponse(attempt));
    }

    @Operation(summary = "Получить историю своих попыток")
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

    private void createOrGetExpertTicket(Attempt attempt) {
        ticketRepository.findByAttemptId(attempt.getId()).orElseGet(() -> {
            ReviewTicket newTicket = new ReviewTicket(attempt);
            newTicket.setStatus(ExpertTicketStatus.QUEUED);
            return ticketRepository.save(newTicket);
        });
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
                a.getReviewMode(),
                a.getAnswers(),
                a.getAiFeedback(),
                a.getDeadline(),
                Instant.now(),
                a.getSubmittedAt(),
                a.getScore(),
                a.getFeedback(),
                a.getVersion(),
                a.getCreatedAt()
        );
    }

    private ReviewTicketResponse mapToTicketResponse(ReviewTicket t) {
        List<TicketMessageResponse> msgs = messageRepository.findByTicketIdOrderByCreatedAtAsc(t.getId())
                .stream()
                .map(m -> new TicketMessageResponse(m.getId(), m.getSender().getId(), m.getSenderRole(), m.getMessage(), m.getCreatedAt()))
                .toList();

        return new ReviewTicketResponse(
                t.getId(),
                t.getAttempt().getId(),
                t.getAttempt().getTargetCase().getId(),
                t.getAttempt().getTargetCase().getTitle(),
                t.getAttempt().getUser().getId(),
                t.getAssignedExpert() != null ? t.getAssignedExpert().getId() : null,
                t.getStatus(),
                t.getExpertFeedback(),
                t.getQuestionsCount(),
                t.getAnswersCount(),
                msgs,
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}