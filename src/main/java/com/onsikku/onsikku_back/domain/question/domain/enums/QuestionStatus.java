package com.onsikku.onsikku_back.domain.question.domain.enums;

public enum QuestionStatus {
  PENDING,
  SENT,
  READ,     // 후순위
  ANSWERED,
  EXPIRED,
  FAILED
}