package com.onsikku.onsikku_back.domain.notification.repository;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.notification.entity.NotificationHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, UUID> {

  // 알림 목록 조회
  Slice<NotificationHistory> findByMemberOrderByPublishedAtDesc(Member member, Pageable pageable);

  // readAt = null 인 필터링 알림 개수 조회
  long countByMemberAndReadAtIsNull(Member member);

  // 사용자 모든 알림 읽음 처리
  @Modifying
  @Query("""
    UPDATE NotificationHistory n SET n.readAt = :now 
    WHERE n.member.id = :memberId AND n.readAt IS NULL
    """)
  int markAllAsReadByMemberId(@Param("memberId") UUID memberId, @Param("now") LocalDateTime now);

  // 사용자의 전체 알림 삭제
  void deleteAllByMember(Member member);
}
