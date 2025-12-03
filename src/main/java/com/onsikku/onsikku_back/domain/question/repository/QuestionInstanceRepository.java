package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionInstanceRepository extends JpaRepository<QuestionInstance, UUID> {
  @Query("SELECT qi FROM QuestionInstance qi LEFT JOIN FETCH qi.template WHERE qi.id = :id")
  Optional<QuestionInstance> findByIdWithQuestionTemplate(UUID id);

  // 특정 가족의 '오늘을 포함한 가장 최신의' QuestionInstance의 ID를 조회합니다.
  @Query("SELECT qi FROM QuestionInstance qi " +
      "WHERE qi.family.id = :familyId AND qi.generatedAt <= :currentDate " +
      "ORDER BY qi.generatedAt DESC")
  List<QuestionInstance> findMostRecentInstance(@Param("familyId") UUID familyId, @Param("currentDate") LocalDateTime currentDate, Pageable pageable);

  /**
   * 특정 가족의 특정 기간 동안의 모든 질문 인스턴스를 조회합니다.
   * @param familyId 가족 ID
   * @param startDate 시작일
   * @param endDate 종료일
   * @return 질문 인스턴스 목록
   */
  @Query("SELECT qi FROM QuestionInstance qi " +
      "WHERE qi.family.id = :familyId AND qi.generatedAt BETWEEN :startDate AND :endDate " +
      "ORDER BY qi.generatedAt DESC")
  List<QuestionInstance> findQuestionsByFamilyIdAndDateTimeRange(@Param("familyId") UUID familyId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  // 가족으로 삭제 (테스트용)
  int deleteAllByFamilyId(UUID familyId);

  List<QuestionInstance> findAllByFamily(Family family);
}
