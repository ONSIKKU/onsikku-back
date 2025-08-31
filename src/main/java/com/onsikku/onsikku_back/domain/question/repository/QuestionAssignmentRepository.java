package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.question.domain.AssignmentState;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionAssignmentRepository extends JpaRepository<QuestionAssignment, UUID> {
  /**
   * 특정 가족의 '가장 오래된 미답변' QuestionInstance의 ID를 조회합니다.
   */
  @Query("SELECT qi.id FROM QuestionAssignment qa " +
      "JOIN qa.instance qi " +
      "WHERE qi.family.id = :familyId AND qa.state <> :answeredState " +
      "ORDER BY qi.plannedDate ASC")
  Optional<UUID> findOldestUnansweredInstanceId(
      @Param("familyId") UUID familyId,
      @Param("answeredState") AssignmentState answeredState,
      Pageable pageable
  );

  /**
   * 특정 가족의 '오늘을 포함한 가장 최신의' QuestionInstance의 ID를 조회합니다.
   */
  @Query("SELECT qi.id FROM QuestionInstance qi " +
      "WHERE qi.family.id = :familyId AND qi.plannedDate <= :currentDate " +
      "ORDER BY qi.plannedDate DESC")
  Optional<UUID> findMostRecentInstanceId(@Param("familyId") UUID familyId, @Param("currentDate") LocalDate currentDate, Pageable pageable);

  /**
   * 최종적으로 결정된 Instance ID로 모든 할당 목록을 조회하는 메서드
   */
  @Query("SELECT qa FROM QuestionAssignment qa " +
      "JOIN FETCH qa.instance qi " +
      "WHERE qi.id = :instanceId")
  List<QuestionAssignment> findAllByInstanceId(@Param("instanceId") UUID instanceId);

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
