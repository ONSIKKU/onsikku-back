package com.onsikku.onsikku_back.domain.notification.event;

import java.util.List;
import java.util.UUID;

public class AnswerCompletedEvent extends NotificationEvent {
    public AnswerCompletedEvent(UUID targetMemberId, String todayMemberName, UUID memberQuestionId) {
        super(targetMemberId, 
              NotificationType.ANSWER_ADDED,  // "오늘의 이야기가 공개되었어요!"
              List.of(todayMemberName)         // args: [주인공 이름] -> "%s님이 답변을..."
        );
        
        addMemberQuestionIdPayload(memberQuestionId);
    }
}