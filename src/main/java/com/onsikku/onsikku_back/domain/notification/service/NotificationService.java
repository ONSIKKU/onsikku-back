package com.onsikku.onsikku_back.domain.notification.service;

import com.google.firebase.messaging.*;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.notification.entity.FcmToken;
import com.onsikku.onsikku_back.domain.notification.entity.NotificationHistory;
import com.onsikku.onsikku_back.domain.notification.event.NotificationType;
import com.onsikku.onsikku_back.domain.notification.repository.FcmTokenRepository;
import com.onsikku.onsikku_back.domain.notification.repository.NotificationHistoryRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  // FCM Token
  private static final long FCM_TOKEN_TTL = 30 * 24 * 60 * 60L; // FCM 토큰 30일 후 파기
  // Notification
  private static final String NOTIFICATION_ICON_PATH = "/onsikku-logo.png";

  private final FcmTokenRepository fcmTokenRepository;
  private final NotificationHistoryRepository notificationHistoryRepository;
  private final MemberRepository memberRepository;

  // 단일 사용자 알림 전송
  public void sendToMember(UUID memberId, NotificationType type, List<String> args, Map<String, String> payload) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

    // 1. 메시지 포맷팅
    String title = String.format(type.getTitle(), args.toArray());
    String body = String.format(type.getBody(), args.toArray());

    // 알림 히스토리 저장
    try {
      saveNotificationHistory(member, type, title, body, payload);
    } catch (Exception e) {
      log.error("알림 히스토리 저장 실패: memberId={}, title={}", memberId, title, e);
      // 히스토리 저장 실패 시에도 FCM 알림 발송 진행
    }

    // FCM 알림 전송
    List<String> tokens = fcmTokenRepository.findAllByMember(member).stream()
        .map(FcmToken::getToken)
        .collect(Collectors.toList());
    if (!tokens.isEmpty()) {
      sendFcmMulticast(tokens, title, body, payload);
    }
  }

  // 다중 기기 알림 전송 (멀티캐스트)
  private void sendFcmMulticast(List<String> tokens, String title, String body, Map<String, String> payload) {
    try {
      // Android 세부 설정
      AndroidConfig androidConfig = AndroidConfig.builder()
          .setNotification(AndroidNotification.builder().setImage(NOTIFICATION_ICON_PATH).build())
          .build();

      // iOS 세부 설정
      ApnsConfig apnsConfig = ApnsConfig.builder()
          .setAps(Aps.builder().setMutableContent(true).build())
          .setFcmOptions(ApnsFcmOptions.builder().setImage(NOTIFICATION_ICON_PATH).build())
          .build();

      Notification notification = Notification.builder()
          .setTitle(title)
          .setBody(body)
          .build();

      MulticastMessage message = MulticastMessage.builder()
          .addAllTokens(tokens)
          .setNotification(notification)
          .setAndroidConfig(androidConfig)
          .setApnsConfig(apnsConfig)
          .putAllData(payload)
          .build();

      BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
      log.debug("FCM Multicast 전송 완료: 성공 {}건, 실패 {}건", response.getSuccessCount(), response.getFailureCount());

    } catch (FirebaseMessagingException e) {
      log.error("FCM 전송 중 오류 발생: {}", e.getMessage());
    }
  }

  private void saveNotificationHistory(Member member, NotificationType type, String title, String body, Map<String, String> payload) {
    NotificationHistory history = NotificationHistory.builder()
        .member(member)
        .notificationType(type)
        .title(title)
        .body(body)
        .payload(payload)
        .publishedAt(LocalDateTime.now())
        .deepLink(payload.get("deepLink"))
        .confirmedAt(null)
        .readAt(null)
        .build();
    notificationHistoryRepository.save(history);
    log.debug("알림 히스토리 저장 완료: notificationHistoryId={}, memberId={}, notificationType={}", history.getNotificationHistoryId(), member.getId(), type.name());
  }
}
