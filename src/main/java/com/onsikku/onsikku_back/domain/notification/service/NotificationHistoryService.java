package com.onsikku.onsikku_back.domain.notification.service;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.notification.dto.NotificationHistoryResponse;
import com.onsikku.onsikku_back.domain.notification.entity.NotificationHistory;
import com.onsikku.onsikku_back.domain.notification.repository.NotificationHistoryRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationHistoryService {
  private final NotificationHistoryRepository notificationHistoryRepository;

  // ------------------------ 조회 메서드 ------------------------
  // 사용자별 알림 목록 조회
  @Transactional(readOnly = true)
  public NotificationHistoryResponse getNotificationHistorySlice(Member member, int page, int size) {
    log.debug("사용자: {} 의 알림 목록 Slice 조회 (page: {}, size: {})", member.getId(), page, size);
    return NotificationHistoryResponse.builder()      // Slice로 조회 (Count 쿼리 실행 X)
        .notificationHistorySlice(notificationHistoryRepository.findByMemberOrderByPublishedAtDesc(member, PageRequest.of(page, size)))
        .build();
  }

  // 안읽은 알림 개수 조회
  @Transactional(readOnly = true)
  public NotificationHistoryResponse getUnReadNotificationCount(Member member) {
    Long unReadCount = notificationHistoryRepository.countByMemberAndReadAtIsNull(member);
    log.debug("사용자: {} 의 안읽은 알림 개수: {}", member.getId(), unReadCount);
    return NotificationHistoryResponse.builder()
      .unReadCount(unReadCount)
      .build();
  }

  // ------------------------ 읽음/확인 처리 메서드 ------------------------
  // 특정 알림 확인 처리
  @Transactional
  public void markAsConfirmed(Member member, UUID notificationHistoryId) {
    NotificationHistory notificationHistory = findNotificationHistoryById(notificationHistoryId);
    validateReceiver(notificationHistory, member);
    notificationHistory.markAsConfirmed();
    log.debug("알림: {} 읽음 처리", notificationHistory.getId());
  }

  // 모든 알림 읽음 처리
  @Transactional
  public void markAllAsRead(Member member) {
    int updatedCount = notificationHistoryRepository.markAllAsReadByMemberId(member.getId(), LocalDateTime.now());
    log.debug("사용자: {} 의 모든 알림 읽음 처리. 처리 개수: {} 개", member.getId(), updatedCount);
  }

  // ------------------------ 삭제 메서드 ------------------------
  // 알림 삭제 (Hard Delete)
  @Transactional
  public void deleteNotification(Member member, UUID notificationHistoryId) {
    NotificationHistory notificationHistory = findNotificationHistoryById(notificationHistoryId);

    validateReceiver(notificationHistory, member);

    notificationHistoryRepository.delete(notificationHistory);
    log.debug("알림 삭제 완료: notificationHistoryId={}", notificationHistory.getId());
  }

  // 전체 알림 삭제 (Hard Delete)
  @Transactional
  public void deleteAllNotifications(Member member) {
    notificationHistoryRepository.deleteAllByMember(member);
    log.debug("사용자: {} 의 모든 알림 삭제 완료", member.getId());
  }

  // ------------------------ helper method ------------------------

  // Id 기반 알림 조회
  private NotificationHistory findNotificationHistoryById(UUID notificationHistoryId) {
    return notificationHistoryRepository.findById(notificationHistoryId)
      .orElseThrow(() -> {
        log.error("UUID: {}에 해당하는 알림을 찾을 수 없습니다", notificationHistoryId);
        return new BaseException(BaseResponseStatus.NOTIFICATION_HISTORY_NOT_FOUND);
      });
  }

  // 알림 수신자 (소유자) 검증
  private void validateReceiver(NotificationHistory notificationHistory, Member member) {
    if (!notificationHistory.getMember().getId().equals(member.getId())) {
      log.error("알림의 수신자(소유자)만 접근 가능합니다");
      throw new BaseException(BaseResponseStatus.INVALID_NOTIFICATION_HISTORY_OWNER);
    }
  }
}
