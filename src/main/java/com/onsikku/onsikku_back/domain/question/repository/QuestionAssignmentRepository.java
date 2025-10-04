package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.question.domain.AssignmentState;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionAssignmentRepository extends JpaRepository<QuestionAssignment, UUID> {
  QuestionAssignment findTop1ByMemberCreatedAtAfter(LocalDateTime after);

  /**
   * 특정 가족의 '가장 오래된 미답변' QuestionInstance의 ID를 조회합니다.
   */
  @Query("SELECT qi.id FROM QuestionAssignment qa " +
      "JOIN qa.questionInstance qi " +
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
      "JOIN FETCH qa.questionInstance qi " +
      "WHERE qi.id = :instanceId")
  List<QuestionAssignment> findAllByInstanceId(@Param("instanceId") UUID instanceId);

  @Query("SELECT qa FROM QuestionAssignment qa " +
      "JOIN FETCH qa.member " +
      "JOIN FETCH qa.questionInstance " +
      "WHERE qa.questionInstance.id IN :instanceIds " +
      "ORDER BY qa.questionInstance.plannedDate DESC")
  List<QuestionAssignment> findByInstanceIdsWithMembersAndQuestionInstance(@Param("instanceIds") List<UUID> instanceIds);


  @Query("SELECT COUNT(qa) FROM QuestionAssignment qa WHERE qa.member.id = :memberId AND qa.sentAt > :aMonthAgo")
  int countByMemberIdAndSentAtAfter(UUID memberId, LocalDateTime aMonthAgo);
}