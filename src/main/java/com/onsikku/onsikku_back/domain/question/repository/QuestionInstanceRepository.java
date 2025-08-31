package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionInstanceRepository extends JpaRepository<QuestionInstanceRepository, Long> {
  /**
   * 특정 가족의 특정 기간 동안의 모든 질문 인스턴스를 조회합니다.
   * N+1 문제를 방지하기 위해 subject(Member)를 fetch join 합니다.
   * @param familyId 가족 ID
   * @param startDate 시작일
   * @param endDate 종료일
   * @return 질문 인스턴스 목록
   */
  @Query("SELECT qi FROM QuestionInstance qi " +
      "JOIN FETCH qi.subject " +
      "WHERE qi.family.id = :familyId " +
      "AND qi.plannedDate BETWEEN :startDate AND :endDate " +
      "ORDER BY qi.plannedDate DESC")
  List<QuestionInstance> findByFamilyIdAndPlannedDateBetween(
      @Param("familyId") UUID familyId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
}
