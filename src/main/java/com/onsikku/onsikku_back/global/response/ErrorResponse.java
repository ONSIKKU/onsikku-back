package com.onsikku.onsikku_back.global.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ErrorResponse {

  private BaseResponseStatus baseResponseStatus;
  private int code;
  private String errorMessage;

  public ErrorResponse(BaseResponseStatus baseResponseStatus) {
    this.baseResponseStatus = baseResponseStatus;
    this.code = baseResponseStatus.getCode();
    this.errorMessage = baseResponseStatus.getMessage();
  }
}