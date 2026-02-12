package com.onsikku.onsikku_back.domain.notification.event;

import java.util.List;
import java.util.UUID;


// 노크 이벤트 TODO : 향후 senderId 추가 가능
public class KnockEvent extends NotificationEvent {
    public KnockEvent(UUID targetMemberId, String senderName, UUID memberQuestionId) {
        super(targetMemberId, NotificationType.KNOCK_KNOCK, List.of(senderName));
        addMemberQuestionIdPayload(memberQuestionId);
    }
}