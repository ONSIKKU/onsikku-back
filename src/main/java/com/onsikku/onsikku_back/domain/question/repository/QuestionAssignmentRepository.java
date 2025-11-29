package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionAssignmentRepository extends JpaRepository<QuestionAssignment, UUID> {
  // 리마인드 / 파기 대상 조회 (sentAt이 특정 시점보다 오래되었고, 상태가 SENT인 모든 할당)
  @Query("SELECT qa FROM QuestionAssignment qa WHERE qa.sentAt < :cutoff AND qa.state IN ('SENT','READ')")
  List<QuestionAssignment> findAssignmentsForSentAtAndSentState(@Param("cutoff") LocalDateTime cutoff);

  /**
   * 특정 가족의 '가장 오래된 미답변' QuestionAssignment ID를 조회합니다.
   */
  @Query("SELECT qi FROM QuestionAssignment qa JOIN qa.questionInstance qi " +
      "WHERE qi.family.id = :familyId AND qa.state IN ('SENT','READ') " +
      "ORDER BY qi.generatedAt ASC")
  List<QuestionInstance> findOldestUnansweredInstance(@Param("familyId") UUID familyId, Pageable pageable);

  // 최종적으로 결정된 Instance ID로 모든 할당 목록을 조회하는 메서드
  @Query("SELECT qa FROM QuestionAssignment qa JOIN FETCH qa.questionInstance qi JOIN FETCH qa.member m JOIN FETCH m.family WHERE qi.id = :instanceId")
  List<QuestionAssignment> findAllByInstanceId(@Param("instanceId") UUID instanceId);

  // 여러 Instance ID로 할당 목록을 조회하는 메서드
  @Query("SELECT qa FROM QuestionAssignment qa JOIN FETCH qa.member WHERE qa.questionInstance.id IN :instanceIds ORDER BY qa.sentAt DESC")
  List<QuestionAssignment> findAllByInstanceIdsWithMembers(@Param("instanceIds") List<UUID> instanceIds);

  // 특정 멤버가 최근 한 달간 받은 QuestionAssignment의 개수를 조회합니다.
  @Query("SELECT COUNT(qa) FROM QuestionAssignment qa WHERE qa.member.id = :memberId AND qa.sentAt > :aMonthAgo")
  int countByMemberIdAndSentAtAfter(UUID memberId, LocalDateTime aMonthAgo);

  // 특정 가족의 '오늘을 포함한 가장 최신 미답변' QuestionAssignment의 ID를 조회합니다.
  @Query("SELECT qa FROM QuestionAssignment qa " +
      "WHERE qa.family.id = :familyId AND qa.sentAt <= :currentDate AND qa.state IN ('SENT', 'READ')" +
      "ORDER BY qa.sentAt DESC")
  List<QuestionAssignment> findMostRecentUnansweredAssignmentId(@Param("familyId") UUID familyId, @Param("currentDate") LocalDateTime currentDate);

  // 가족으로 삭제 (테스트용)
  int deleteAllByFamily(Family family);

  void deleteAllByMember(Member member);
}