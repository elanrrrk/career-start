package com.platform.api.controller;

import com.platform.api.dto.PageResponse;
import com.platform.api.dto.ReviewRequest;
import com.platform.api.dto.ReviewTicketResponse;
import com.platform.api.dto.TicketMessageRequest;
import com.platform.api.dto.TicketMessageResponse;
import com.platform.api.entity.*;
import com.platform.api.event.AuditEvent;
import com.platform.api.repository.AttemptRepository;
import com.platform.api.repository.ReviewTicketRepository;
import com.platform.api.repository.TicketMessageRepository;
import com.platform.api.repository.UserRepository;
import com.platform.api.service.NotificationService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Tag(name = "Expert Review & Queue", description = "Воронка проверки решений экспертом, управление очередью и диалоги")
@RestController
@RequestMapping("/api/v1/expert/tickets")
@PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
public class ExpertTicketController {

    private final ReviewTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AttemptRepository attemptRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public ExpertTicketController(
            ReviewTicketRepository ticketRepository,
            TicketMessageRepository messageRepository,
            UserRepository userRepository,
            AttemptRepository attemptRepository,
            NotificationService notificationService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
    }

    @Operation(summary = "Получить общую очередь заявок по статусу (QUEUED, IN_PROGRESS и др.)")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<PageResponse<ReviewTicketResponse>> getTickets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "QUEUED") ExpertTicketStatus status
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, limit, Sort.by("createdAt").ascending());

        Page<ReviewTicket> ticketsPage = ticketRepository.findByStatus(status, pageable);
        var items = ticketsPage.getContent().stream().map(this::mapToResponse).toList();

        return ResponseEntity.ok(new PageResponse<>(items, ticketsPage.getTotalElements(), page, limit));
    }

    @Operation(summary = "Личный кабинет эксперта: получить закрепленные за мной заявки и вопросы")
    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ResponseEntity<PageResponse<ReviewTicketResponse>> getMyTickets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) ExpertTicketStatus status,
            Authentication authentication
    ) {
        UUID expertId = UUID.fromString((String) authentication.getPrincipal());
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, limit, Sort.by("updatedAt").descending());

        Page<ReviewTicket> ticketsPage = (status != null)
                ? ticketRepository.findByAssignedExpertIdAndStatus(expertId, status, pageable)
                : ticketRepository.findByAssignedExpertId(expertId, pageable);

        var items = ticketsPage.getContent().stream().map(this::mapToResponse).toList();
        return ResponseEntity.ok(new PageResponse<>(items, ticketsPage.getTotalElements(), page, limit));
    }

    @Operation(summary = "Получить заявку с полной историей сообщений")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ReviewTicketResponse> getTicketById(@PathVariable UUID id) {
        ReviewTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тикет не найден"));
        return ResponseEntity.ok(mapToResponse(ticket));
    }

    @Operation(summary = "Взять заявку в работу (Статус: IN_PROGRESS)")
    @PostMapping("/{id}/claim")
    @Transactional
    public ResponseEntity<ReviewTicketResponse> claimTicket(@PathVariable UUID id, Authentication authentication) {
        UUID expertId = UUID.fromString((String) authentication.getPrincipal());
        User expert = userRepository.findById(expertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        ReviewTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тикет не найден"));

        // Защита: эксперт не может проверять свою же работу
        if (ticket.getAttempt().getUser().getId().equals(expertId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете проверять собственное решение");
        }

        if (ticket.getStatus() != ExpertTicketStatus.QUEUED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заявку можно взять в работу только из статуса QUEUED");
        }

        ticket.setStatus(ExpertTicketStatus.IN_PROGRESS);
        ticket.setAssignedExpert(expert);
        ticketRepository.save(ticket);

        eventPublisher.publishEvent(new AuditEvent(
                expertId,
                "CLAIM_TICKET",
                "ReviewTicket",
                ticket.getId().toString(),
                "Эксперт взял заявку в работу"
        ));

        return ResponseEntity.ok(mapToResponse(ticket));
    }

    @Operation(summary = "Вернуть заявку обратно в общую очередь (Статус: QUEUED)")
    @PostMapping("/{id}/release")
    @Transactional
    public ResponseEntity<ReviewTicketResponse> releaseTicket(@PathVariable UUID id, Authentication authentication) {
        UUID expertId = UUID.fromString((String) authentication.getPrincipal());

        ReviewTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тикет не найден"));

        if (ticket.getStatus() != ExpertTicketStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "В очередь можно вернуть только заявку в статусе IN_PROGRESS");
        }

        // Защита BOLA
        if (ticket.getAssignedExpert() != null && !ticket.getAssignedExpert().getId().equals(expertId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Заявку может вернуть только назначенный эксперт");
        }

        ticket.setStatus(ExpertTicketStatus.QUEUED);
        ticket.setAssignedExpert(null);
        ticketRepository.save(ticket);

        eventPublisher.publishEvent(new AuditEvent(
                expertId,
                "RELEASE_TICKET",
                "ReviewTicket",
                ticket.getId().toString(),
                "Заявка возвращена в общую очередь"
        ));

        return ResponseEntity.ok(mapToResponse(ticket));
    }

    @Operation(summary = "Отправить первичную оценку с разбором (Статус: REVIEW_SENT -> EXPERT_REVIEW_RECEIVED)")
    @PostMapping("/{id}/review")
    @Transactional
    public ResponseEntity<ReviewTicketResponse> sendReview(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRequest req,
            Authentication authentication
    ) {
        UUID expertId = UUID.fromString((String) authentication.getPrincipal());

        ReviewTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тикет не найден"));

        // Защита BOLA: оценивать может только назначенный эксперт
        if (ticket.getAssignedExpert() == null || !ticket.getAssignedExpert().getId().equals(expertId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Оценивать работу может только назначенный эксперт");
        }

        if (ticket.getStatus() != ExpertTicketStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Оценку можно отправить только для заявки со статусом IN_PROGRESS");
        }

        ticket.setStatus(ExpertTicketStatus.REVIEW_SENT);
        ticket.setExpertFeedback(req.feedback());
        ticketRepository.save(ticket);

        Attempt attempt = ticket.getAttempt();
        attempt.setStatus(AttemptStatus.EXPERT_REVIEW_RECEIVED);
        attempt.setScore(req.score());
        attempt.setFeedback(req.feedback());
        attemptRepository.save(attempt);

        // SSE Push-уведомление студенту о готовности проверки
        notificationService.sendNotification(
                attempt.getUser().getId(),
                "REVIEW_RECEIVED",
                "Эксперт проверил вашу работу по кейсу " + attempt.getTargetCase().getTitle() + ". Баллы: " + req.score()
        );

        eventPublisher.publishEvent(new AuditEvent(
                expertId,
                "SEND_REVIEW",
                "ReviewTicket",
                ticket.getId().toString(),
                "Эксперт выставил " + req.score() + " баллов и отправил разбор"
        ));

        return ResponseEntity.ok(mapToResponse(ticket));
    }

    @Operation(summary = "Ответить на вопрос студента (Статус: ANSWER_SENT / FOLLOW_UP_ANSWER_SENT)")
    @PostMapping("/{id}/answers")
    @Transactional
    public ResponseEntity<ReviewTicketResponse> replyToQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody TicketMessageRequest req,
            Authentication authentication
    ) {
        UUID expertId = UUID.fromString((String) authentication.getPrincipal());
        User expert = userRepository.findById(expertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        ReviewTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тикет не найден"));

        // Защита BOLA: отвечать на апелляцию может только назначенный эксперт
        if (ticket.getAssignedExpert() == null || !ticket.getAssignedExpert().getId().equals(expertId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Отвечать на вопрос может только назначенный эксперт");
        }

        if (ticket.getStatus() != ExpertTicketStatus.QUESTION_RECEIVED &&
            ticket.getStatus() != ExpertTicketStatus.FOLLOW_UP_QUESTION_RECEIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ответить можно только при наличии входящего вопроса");
        }

        int answerCount = ticket.getAnswersCount() + 1;
        ticket.setAnswersCount(answerCount);

        if (answerCount == 1) {
            ticket.setStatus(ExpertTicketStatus.ANSWER_SENT);
        } else {
            ticket.setStatus(ExpertTicketStatus.FOLLOW_UP_ANSWER_SENT);
        }

        ticketRepository.save(ticket);

        Attempt attempt = ticket.getAttempt();
        attempt.setStatus(AttemptStatus.EXPERT_ANSWER_RECEIVED);
        attemptRepository.save(attempt);

        TicketMessage msg = new TicketMessage(ticket, expert, MessageSenderRole.EXPERT, req.message());
        messageRepository.save(msg);

        // SSE Push-уведомление студенту об ответе на апелляцию
        notificationService.sendNotification(
                attempt.getUser().getId(),
                "ANSWER_RECEIVED",
                "Эксперт ответил на ваш вопрос по кейсу " + attempt.getTargetCase().getTitle()
        );

        eventPublisher.publishEvent(new AuditEvent(
                expertId,
                answerCount == 1 ? "SEND_ANSWER" : "SEND_FOLLOW_UP_ANSWER",
                "ReviewTicket",
                ticket.getId().toString(),
                "Эксперт отправил ответ #" + answerCount
        ));

        return ResponseEntity.ok(mapToResponse(ticket));
    }

    private ReviewTicketResponse mapToResponse(ReviewTicket t) {
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