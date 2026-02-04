package com.onsikku.onsikku_back.global.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
  private boolean isRegistered;
  private String accessToken;
  private String refreshToken;
  private String registrationToken;
  private String ticket;
}