package com.platform.api.repository;

import com.platform.api.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CaseRepository extends JpaRepository<Case, UUID> {

    // Стандартная выборка опубликованных кейсов для клиентов без поисковой строки
    Page<Case> findByIsPublishedTrue(Pageable pageable);

    // Поиск по названию или категории среди опубликованных кейсов
    @Query("SELECT c FROM Case c WHERE c.isPublished = true AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.category) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Case> searchPublishedCases(@Param("q") String q, Pageable pageable);

    // Поиск по всем кейсам для экспертов и администраторов
    @Query("SELECT c FROM Case c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.category) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Case> searchAllCasesAdmin(@Param("q") String q, Pageable pageable);
}