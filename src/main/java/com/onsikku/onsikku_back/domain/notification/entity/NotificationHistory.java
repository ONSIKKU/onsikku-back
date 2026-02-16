package com.onsikku.onsikku_back.domain.notification.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.notification.event.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(callSuper = true)
public class NotificationHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType notificationType;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String body;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Map<String, String> payload;

  // 알림 클릭 시 이동할 페이지
  private String deepLink;

  // 알림 목록을 확인한 시각 (레드닷 제거용)
  private LocalDateTime readAt;

  // 실제 알림을 클릭해서 상세 페이지로 이동한 시각
  private LocalDateTime confirmedAt;

  @Column(nullable = false)
  private LocalDateTime publishedAt;

  private void markAsRead() {
    if (this.readAt == null) {
      this.readAt = LocalDateTime.now();
    }
  }

  public void markAsConfirmed() {
    this.confirmedAt = LocalDateTime.now();
    markAsRead();   // 확인했다면 읽음 처리도 함께 보장 (레드 닷 제거용)
  }
}
