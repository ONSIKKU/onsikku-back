package com.onsikku.onsikku_back.global.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonProperty("isRegistered")   // redis 저장로직에서 직렬화, 역직렬화 시 is가 사라져 오류 발생, 이후 이름 고정
  private boolean isRegistered;
  private String accessToken;
  private String refreshToken;
  private String registrationToken;
  private String ticket;
}