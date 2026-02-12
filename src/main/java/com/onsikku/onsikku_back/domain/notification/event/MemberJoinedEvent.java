package com.onsikku.onsikku_back.domain.notification.event;

import java.util.List;
import java.util.UUID;

public class MemberJoinedEvent extends NotificationEvent {
    public MemberJoinedEvent(UUID targetMemberId, String joinedMemberName) {
        super(targetMemberId, 
              NotificationType.MEMBER_JOINED,
              List.of(joinedMemberName)
        );
    }
}