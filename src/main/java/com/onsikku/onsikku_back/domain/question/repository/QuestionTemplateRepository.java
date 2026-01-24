package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface QuestionTemplateRepository extends JpaRepository<Question, String> {
  @Query("SELECT qt FROM Question qt " +
      "WHERE qt.isActive = true " + // 활성 템플릿만
      "AND qt.id NOT IN (" +
      "SELECT qi.template.id FROM QuestionInstance qi " +
      "WHERE qi.family.id = :familyId AND qi.generatedAt >= :startDate" +
      ")")
  List<Question> findUnusedTemplatesRecentlyByFamily(
      @Param("familyId") UUID familyId,
      @Param("startDate") LocalDateTime startDate
  );
}
