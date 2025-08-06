package com.onsikku.onsikku_back.global.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ErrorResponse {

  private BaseResponseStatus baseResponseStatus;
  private String errorMessage;
}