package com.onsikku.onsikku_back.domain.answer.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {
  SPAM("스팸 및 홍보성 콘텐츠"),
  INAPPROPRIATE_CONTENT("부적절한 콘텐츠 (음란물 등)"),
  ABUSIVE_LANGUAGE("욕설 및 비방, 혐오 표현"),
  PRIVACY_VIOLATION("개인정보 노출"),
  OTHER("기타");

  private final String description;
}
