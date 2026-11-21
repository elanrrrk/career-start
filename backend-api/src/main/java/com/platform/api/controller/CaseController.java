package com.platform.api.controller;

import com.platform.api.dto.CaseRequest;
import com.platform.api.dto.CaseResponse;
import com.platform.api.dto.PageResponse;
import com.platform.api.entity.Case;
import com.platform.api.entity.User;
import com.platform.api.repository.CaseRepository;
import com.platform.api.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;

@Tag(name = "Cases", description = "Управление кейсами и каталог")
@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    public CaseController(CaseRepository caseRepository, UserRepository userRepository) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Получить каталог кейсов с пагинацией и поиском")
    @GetMapping
    public ResponseEntity<PageResponse<CaseResponse>> getCases(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "createdAt") String sort,
        @RequestParam(required = false) String q
    ) {
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, limit, Sort.by(sort).descending());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isStaff = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_EXPERT"));

        boolean hasQuery = (q != null && !q.trim().isBlank());

        Page<Case> casePage;
        if (isStaff) {
            casePage = hasQuery 
                ? caseRepository.searchAllCasesAdmin(q.trim(), pageable) 
                : caseRepository.findAll(pageable);
        } else {
            casePage = hasQuery 
                ? caseRepository.searchPublishedCases(q.trim(), pageable) 
                : caseRepository.findByIsPublishedTrue(pageable);
        }

        var items = casePage.getContent().stream().map(this::mapToResponse).toList();

        return ResponseEntity.ok(new PageResponse<>(
            items,
            casePage.getTotalElements(),
            page,
            limit
        ));
    }

    @Operation(summary = "Получить кейс по ID")
    @GetMapping("/{id}")
    public ResponseEntity<CaseResponse> getCaseById(@PathVariable UUID id) {
        Case c = caseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Кейс не найден"));

        return ResponseEntity.ok(mapToResponse(c));
    }

    @Operation(summary = "Создать новый кейс (только EXPERT или ADMIN)")
    @PostMapping
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CaseRequest req, Authentication authentication) {
        UUID authorId = UUID.fromString((String) authentication.getPrincipal());
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));

        Case c = new Case(req.title(), req.description(), req.category(), req.tags(), req.timeLimitMinutes(), author);
        caseRepository.save(c);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(c));
    }

    @Operation(summary = "Обновить кейс с контролем версии Optimistic Locking (только EXPERT или ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<CaseResponse> updateCase(
        @PathVariable UUID id,
        @Valid @RequestBody CaseRequest req,
        @RequestParam Long version
    ) {
        Case c = caseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Кейс не найден"));

        // Валидация оптимистической блокировки
        if (!Objects.equals(c.getVersion(), version)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Конфликт версий: запись была изменена другим пользователем. Текущая версия: " + c.getVersion()
            );
        }

        c.setTitle(req.title());
        c.setDescription(req.description());
        c.setCategory(req.category());
        c.setTags(req.tags());
        c.setTimeLimitMinutes(req.timeLimitMinutes());

        caseRepository.save(c);

        return ResponseEntity.ok(mapToResponse(c));
    }

    @Operation(summary = "Удалить кейс (только EXPERT или ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<Void> deleteCase(@PathVariable UUID id) {
        if (!caseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Кейс не найден");
        }
        caseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Опубликовать кейс (только EXPERT или ADMIN)")
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('EXPERT', 'ADMIN')")
    public ResponseEntity<CaseResponse> publishCase(@PathVariable UUID id) {
        Case c = caseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Кейс не найден"));

        c.setPublished(true);
        caseRepository.save(c);

        return ResponseEntity.ok(mapToResponse(c));
    }

    private CaseResponse mapToResponse(Case c) {
        return new CaseResponse(
            c.getId(),
            c.getTitle(),
            c.getDescription(),
            c.getCategory(),
            c.getTags(),
            c.isPublished(),
            c.getTimeLimitMinutes(),
            c.getAuthor().getId(),
            c.getVersion(),
            c.getCreatedAt(),
            c.getUpdatedAt()
        );
    }
}