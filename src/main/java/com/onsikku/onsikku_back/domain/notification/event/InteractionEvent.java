package com.onsikku.onsikku_back.domain.notification.event;

import java.util.List;
import java.util.UUID;

// 소통 이벤트 (댓글, 반응)
public class InteractionEvent extends NotificationEvent {
    public InteractionEvent(UUID targetMemberId, NotificationType type, List<String> args, UUID memberQuestionId) {
        super(targetMemberId, type, args);
        addMemberQuestionIdPayload(memberQuestionId);
    }
}