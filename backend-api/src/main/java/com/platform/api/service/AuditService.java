package com.platform.api.service;

import com.platform.api.entity.AuditLog;
import com.platform.api.event.AuditEvent;
import com.platform.api.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAuditEvent(AuditEvent event) {
        try {
            AuditLog auditLog = new AuditLog(
                    event.actorId(),
                    event.action(),
                    event.entityName(),
                    event.entityId(),
                    event.details()
            );
            auditLogRepository.save(auditLog);
            log.info("Аудит сохранен в БД: action={}, entity={}:{}", event.action(), event.entityName(), event.entityId());
        } catch (Exception ex) {
            log.error("Сбой асинхронной записи аудита в БД для действия {}: {}", event.action(), ex.getMessage());
        }
    }
}