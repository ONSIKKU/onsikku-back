package com.onsikku.onsikku_back.global.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
  private boolean isRegistered;
  private String accessToken;
  private String refreshToken;
  private String registrationToken;
}