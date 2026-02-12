package com.onsikku.onsikku_back.domain.notification.event;

import java.util.List;
import java.util.UUID;

// 오늘의 질문 이벤트 (주인공 여부에 따라 분기)
public class DailyQuestionEvent extends NotificationEvent {
    public DailyQuestionEvent(UUID targetMemberId, UUID memberQuestionId, boolean isTargetMember, String targetMemberName) {
        super(targetMemberId, 
              isTargetMember ? NotificationType.TODAY_TARGET_MEMBER : NotificationType.TODAY_TARGET_MEMBER_ANNOUNCED,
              isTargetMember ? List.of() : List.of(targetMemberName));

        addMemberQuestionIdPayload(memberQuestionId);
    }
}