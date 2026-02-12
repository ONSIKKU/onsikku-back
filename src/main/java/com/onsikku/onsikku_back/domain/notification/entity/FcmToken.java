package com.onsikku.onsikku_back.domain.notification.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.notification.dto.FcmTokenRequest;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FcmToken extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID fcmTokenId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  private String token;

  private DeviceType deviceType;

  public static FcmToken createFromRequest(FcmTokenRequest request) {
    return FcmToken.builder()
        .token(request.getFcmToken())
        .member(request.getMember())
        .deviceType(request.getDeviceType())
        .build();
  }

  public void updateToken(String token) {
    this.token = token;
  }
}