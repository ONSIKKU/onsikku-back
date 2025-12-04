package com.onsikku.onsikku_back.global.notification.dto;

import java.util.Map;
import java.util.UUID;

public record NotificationTarget(UUID targetId, NotificationType type) {
    public Map<String, String> toDataMap() {
        return Map.of(
            "targetId", targetId.toString(),
            "type", type.name()
        );
    }
    public static NotificationTarget from(UUID targetId, NotificationType type) {
        return new NotificationTarget(targetId, type);
    }
}

