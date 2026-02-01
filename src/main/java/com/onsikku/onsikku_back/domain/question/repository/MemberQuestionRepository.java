package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.question.domain.MemberQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberQuestionRepository extends JpaRepository<MemberQuestion, UUID> {
  // 주인공의 '오늘을 포함한 가장 최신의' MemberQuestion을 조회합니다.
  @Query("SELECT mq FROM MemberQuestion mq LEFT JOIN FETCH mq.member " +
      "WHERE mq.family.id = :familyId AND mq.sentAt <= :currentDate " +
      "ORDER BY mq.sentAt DESC")
  List<MemberQuestion> findTodayQuestionForFamily(@Param("familyId") UUID familyId, @Param("currentDate") LocalDateTime currentDate, Pageable pageable);

  @Query("SELECT mq FROM MemberQuestion mq LEFT JOIN FETCH mq.member WHERE mq.id = :memberQuestionId")
  Optional<MemberQuestion> findByIdWithMember(@Param("memberQuestionId") UUID memberQuestionId);

  // 주인공 질문 할당 : 대기 중인(PENDING) 질문 중, 레벨과 우선순위에 맞는 가장 적절한 질문 조회
  @Query(value = "SELECT * FROM member_question WHERE member_id = :memberId " +
      "AND question_status = :status AND level IN (:levels) " +
      "ORDER BY priority DESC, created_at ASC " +
      "LIMIT 1", nativeQuery = true)
  Optional<MemberQuestion> findTopQuestionNative(@Param("memberId") UUID memberId, @Param("status") String status, @Param("levels") List<Integer> levels);

  /**
   * 특정 가족의 특정 기간 동안의 모든 질문 인스턴스를 조회합니다.
   * @param familyId 가족 ID
   * @param startDate 시작일
   * @param endDate 종료일
   * @return 질문 인스턴스 목록
   */
  @Query("SELECT mq FROM MemberQuestion mq LEFT JOIN FETCH mq.member " +
      "WHERE mq.family.id = :familyId AND mq.sentAt BETWEEN :startDate AND :endDate " +
      "ORDER BY mq.sentAt DESC")
  List<MemberQuestion> findQuestionsByFamilyIdAndDateTimeRange(@Param("familyId") UUID familyId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Modifying
  @Query("DELETE FROM MemberQuestion mq WHERE mq.family.id = :familyId")
  void deleteByFamilyIdBulk(@Param("familyId") UUID familyId);

  List<MemberQuestion> findAllByFamily(Family family);
}
