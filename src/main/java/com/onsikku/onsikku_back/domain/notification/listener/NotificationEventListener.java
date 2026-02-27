package com.onsikku.onsikku_back.domain.notification.listener;

import com.onsikku.onsikku_back.domain.notification.event.NotificationEvent;
import com.onsikku.onsikku_back.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @Async("notificationTaskExecutor")  // 별도 스레드에서 실행 (메인 로직 성능 영향 X)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNotificationEvent(NotificationEvent event) {
    try {
      log.info("알림 이벤트 수신: type={}, target={}", event.getNotificationType(), event.getTargetMemberId());

      notificationService.sendToMember(
          event.getTargetMemberId(),
          event.getNotificationType(),
          event.getArgs(),
          event.getPayload()
      );
    } catch (Exception e) {
      log.error("알림 발송 실패: target={}, type={}, error={}", event.getTargetMemberId(), event.getNotificationType(), e.getMessage());
    }
  }
}