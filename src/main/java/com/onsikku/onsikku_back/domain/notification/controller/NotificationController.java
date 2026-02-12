package com.onsikku.onsikku_back.domain.notification.controller;

import com.onsikku.onsikku_back.domain.notification.dto.NotificationHistoryResponse;
import com.onsikku.onsikku_back.domain.notification.dto.FcmTokenRequest;
import com.onsikku.onsikku_back.domain.notification.service.FcmTokenService;
import com.onsikku.onsikku_back.domain.notification.service.NotificationHistoryService;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "FCM 토큰 관리 및 알림 내역 관련 기능을 제공합니다.")
public class NotificationController {

  private final FcmTokenService fcmTokenService;
  private final NotificationHistoryService notificationHistoryService;

  // -------------------- 알림 내역 관련 --------------------

  @Operation(summary = "알림 설정 해제 (FCM 토큰 삭제)", description = """
      로그인한 사용자의 기기 FCM 토큰을 삭제함으로써, 알림 설정을 해제합니다.
      회원의 알림 설정을 false로 갱신합니다.
  
      ## 반환값
      - 성공 시 상태코드 204 (No Content)와 빈 응답 본문
  
      ## 에러코드
      - **`INVALID_FCM_TOKEN`**: 유효하지 않은 FCM 토큰입니다.
   """)
  @DeleteMapping("/tokens")
  public ResponseEntity<Void> deleteFcmToken(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                             @RequestBody FcmTokenRequest request) {
    request.setMember(customUserDetails.getMember());
    fcmTokenService.deleteFcmTokenAndUpdateMember(request);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "알림 설정 등록 (FCM 토큰 등록/갱신)", description = """
      로그인한 사용자의 기기 FCM 토큰을 저장하거나 최신화합니다.
      회원의 알림 설정을 true로 갱신합니다.
      
      ## 반환값
      - 성공 시 상태코드 204 (No Content)와 빈 응답 본문
  
      ## 유의사항
      - 사용자마다 DeviceType 별로 FCM 토큰 1개씩 저장가능합니다 (iOS, Android, Web)
      - FCM 토큰 자동 만료는 없으며, 갱신 시 새롭게 요청하면 됩니다.
  """)
  @PostMapping("/tokens")
  public ResponseEntity<Void> saveFcmToken(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody FcmTokenRequest request) {
    request.setMember(customUserDetails.getMember());
    fcmTokenService.saveFcmTokenAndUpdateMember(request);
    return ResponseEntity.noContent().build();
  }

  // -------------------- 알림 내역 관련 --------------------

  @Operation(summary = "알림 내역 목록 조회", description = """
  로그인한 사용자의 전체 알림 내역을 조회합니다.
  page 요청을 지원합니다. (?page=1&size=10)
  Slice 기반 응답을 반환합니다. (totalPages는 미존재, hasNext: true 존재)
  """)
  @GetMapping
  public ResponseEntity<NotificationHistoryResponse> getNotificationHistorySlice(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestParam int page, @RequestParam int size) {
    return ResponseEntity.ok(notificationHistoryService.getNotificationHistorySlice(customUserDetails.getMember(), page, size));
  }

  @Operation(summary = "읽지 않은 알림 개수 조회", description = "아직 읽지 않은 알림의 총 개수를 반환합니다.")
  @GetMapping("/count/unread")
  public ResponseEntity<NotificationHistoryResponse> getUnReadNotificationCount(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.ok(notificationHistoryService.getUnReadNotificationCount(customUserDetails.getMember()));
  }

  // -------------------- 알림 읽음/확인 --------------------

  @Operation(summary = "특정 알림 확인 처리", description = "특정 알림을 클릭했을 때 호출합니다. (읽음+확인 처리)")
  @PatchMapping("/{notificationHistoryId}/confirm")
  public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable UUID notificationHistoryId) {
    notificationHistoryService.markAsConfirmed(customUserDetails.getMember(), notificationHistoryId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "모든 알림 읽음 처리", description = "알림 목록 진입 시 호출하여, 사용자의 모든 미확인 알림을 읽음 상태로 일괄 변경합니다.")
  @PatchMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    notificationHistoryService.markAllAsRead(customUserDetails.getMember());
    return ResponseEntity.noContent().build();
  }
  // -------------------- 알림 삭제 --------------------

  @Operation(summary = "특정 알림 삭제", description = "알림 내역에서 특정 알림 하나를 삭제합니다. 본인 알림만 지울 수 있습니다.")
  @DeleteMapping("/{notificationHistoryId}")
  public ResponseEntity<Void> deleteNotification(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable UUID notificationHistoryId) {
    notificationHistoryService.deleteNotification(customUserDetails.getMember(), notificationHistoryId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "전체 알림 삭제", description = "사용자의 모든 알림 내역을 삭제합니다.")
  @DeleteMapping("/all")
  public ResponseEntity<Void> deleteAllNotifications(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    notificationHistoryService.deleteAllNotifications(customUserDetails.getMember());
    return ResponseEntity.noContent().build();
  }
}