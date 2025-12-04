package com.onsikku.onsikku_back.global.notification.dto;

public enum NotificationType {
    NEW_QUESTION_ASSIGNED, // 새로운 질문 할당
    SECURITY_EVENT,        // 보안 관련 이벤트 (로그인/재발급)
    ANSWER_COMPLETED,      // 답변 완료
    NEW_COMMENT,           // 새 댓글
    REMINDER               // 리마인드 (스케줄러 사용)
}