package com.onsikku.onsikku_back.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.notification.entity.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FcmTokenRequest {

  @Schema(hidden = true, description = "회원")
  @JsonIgnore
  private Member member;

  @NotBlank(message = "FCM 토큰을 입력해주세요")
  @Schema(description = "FCM 토큰")
  private String fcmToken; // FCM 토큰

  @Schema(description = "ANDROID, IOS, ETC")
  private DeviceType deviceType;
}