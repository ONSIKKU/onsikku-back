package com.onsikku.onsikku_back.domain.notification.dto;

import com.onsikku.onsikku_back.domain.notification.entity.NotificationHistory;
import lombok.*;
import org.springframework.data.domain.Slice;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NotificationHistoryResponse {
  private Slice<NotificationHistory> notificationHistorySlice;
  private Long unReadCount;
}
