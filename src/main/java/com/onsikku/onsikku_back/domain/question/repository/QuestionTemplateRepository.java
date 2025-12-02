package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.question.domain.QuestionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface QuestionTemplateRepository extends JpaRepository<QuestionTemplate, String> {
  @Query("SELECT qt FROM QuestionTemplate qt " +
      "WHERE qt.isActive = true " + // 활성 템플릿만
      "AND qt.id NOT IN (" +
      "SELECT qi.template.id FROM QuestionInstance qi " +
      "WHERE qi.family.id = :familyId AND qi.generatedAt >= :startDate" +
      ")")
  List<QuestionTemplate> findUnusedTemplatesRecentlyByFamily(
      @Param("familyId") UUID familyId,
      @Param("startDate") LocalDateTime startDate
  );
}
