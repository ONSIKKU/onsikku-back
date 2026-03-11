package com.onsikku.onsikku_back.domain.member.domain;

public enum WithdrawalReason {
    QUESTION_QUALITY_LOW,         // 질문 퀄리티가 낮음
    QUESTIONS_TOO_PERSONAL,       // 질문이 사적/민감하게 느껴짐
    QUESTIONS_TOO_BURDENSOME,     // 질문 답변이 심리적으로 부담됨
    NOT_ENOUGH_FAMILY_ACTIVITY,   // 가족 참여가 적어 앱을 쓰기 어려움
    APP_USABILITY_ISSUE,          // UX가 불편함
    TECHNICAL_ISSUE,              // 버그/성능 이슈
    TOO_MANY_NOTIFICATIONS,       // 알림이 많음
    PRIVACY_CONCERN,              // 개인정보/프라이버시 우려
    FOUND_ALTERNATIVE,            // 대체 서비스 사용
    OTHER                         // 기타
}
