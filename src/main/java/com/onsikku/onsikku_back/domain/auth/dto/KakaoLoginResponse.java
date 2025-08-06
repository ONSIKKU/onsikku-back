package com.onsikku.onsikku_back.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KakaoLoginResponse {
  private boolean isRegistered;
  private String accessToken;
  private String registrationToken;
}