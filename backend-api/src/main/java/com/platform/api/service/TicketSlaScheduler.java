package com.platform.api.service;

import com.platform.api.entity.ExpertTicketStatus;
import com.platform.api.entity.ReviewTicket;
import com.platform.api.event.AuditEvent;
import com.platform.api.repository.ReviewTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TicketSlaScheduler {

    private static final Logger log = LoggerFactory.getLogger(TicketSlaScheduler.class);
    // Лимит времени на проверку экспертом: если за 24 часа нет разбора, тикет возвращается в очередь
    private static final long SLA_HOURS = 24;

    private final ReviewTicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TicketSlaScheduler(ReviewTicketRepository ticketRepository, ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    // Запуск каждые 15 минут
    @Scheduled(fixedRate = 15 * 60 * 1000)
    @Transactional
    public void resetStaleInProgressTickets() {
        Instant threshold = Instant.now().minus(SLA_HOURS, ChronoUnit.HOURS);
        List<ReviewTicket> staleTickets = ticketRepository.findByStatus(
                ExpertTicketStatus.IN_PROGRESS,
                PageRequest.of(0, 100)
        ).getContent();

        for (ReviewTicket ticket : staleTickets) {
            if (ticket.getUpdatedAt().isBefore(threshold)) {
                log.warn("Тикет {} просрочен экспертом (SLA {} ч.). Возврат в очередь.", ticket.getId(), SLA_HOURS);

                ticket.setStatus(ExpertTicketStatus.QUEUED);
                ticket.setAssignedExpert(null);
                ticketRepository.save(ticket);

                eventPublisher.publishEvent(new AuditEvent(
                        ticket.getAttempt().getUser().getId(),
                        "SLA_TIMEOUT_RELEASE",
                        "ReviewTicket",
                        ticket.getId().toString(),
                        "Тикет автоматически возвращен в общую очередь из-за истечения таймаута проверки (24 ч.)"
                ));
            }
        }
    }
}