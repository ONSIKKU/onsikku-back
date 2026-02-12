package com.onsikku.onsikku_back.domain.notification.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
public abstract class NotificationEvent {

  private final UUID targetMemberId;
  private final NotificationType notificationType;
  private final List<String> args; // Title, Body 포맷팅용 인자
  private final Map<String, String> payload;

  protected NotificationEvent(UUID targetMemberId, NotificationType notificationType, List<String> args) {
    this.targetMemberId = targetMemberId;
    this.notificationType = notificationType;
    this.args = args != null ? args : new ArrayList<>();
    this.payload = new HashMap<>();

    // 공통 Payload 설정
    this.payload.put("notificationType", notificationType.name());
    this.payload.put("publishedAt", LocalDateTime.now().toString());
  }

  protected void addAnswerIdPayload(UUID value) {
    if (value != null) {
      this.payload.put("answerId", value.toString());
    }
  }

  protected void addMemberQuestionIdPayload(UUID value) {
    if (value != null) {
      this.payload.put("memberQuestionId", value.toString());
    }
  }
}
