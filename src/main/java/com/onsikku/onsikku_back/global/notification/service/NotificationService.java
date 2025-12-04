package com.onsikku.onsikku_back.global.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.global.notification.domain.FcmToken;
import com.onsikku.onsikku_back.global.notification.repository.FcmTokenRepository;
import com.onsikku.onsikku_back.global.notification.dto.NotificationTarget;
import com.onsikku.onsikku_back.global.notification.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final MemberRepository memberRepository;
  private final FcmTokenRepository fcmTokenRepository;

  // --- [트리거 1] 새로운 질문 할당 시 (스케줄러 호출) ---
  // 새로운 질문이 생성 및 할당되었을 때, 모든 가족 구성원에게 알림을 보냅니다. (QuestionService에서 호출)
  public void notifyNewQuestionAssigned(QuestionInstance questionInstance, List<Member> assignedMembers) {
    log.info("[SYNC] 새로운 질문 할당 알림 처리 시작. QuestionId: {}", questionInstance.getId());
    List<UUID> memberIds = assignedMembers.stream().map(Member::getId).toList();

    String title = "오늘의 질문이 도착했어요! 💬";
    String body = "지금 바로 답변하고 가족에게 나의 생각을 알려주세요.";
    sendNotificationToMemberIds(memberIds, title, body, NotificationTarget.from(questionInstance.getId(), NotificationType.NEW_QUESTION_ASSIGNED));
    log.info("[SYNC] 새로운 질문 할당 알림 발송 완료. 대상 인원: {}", assignedMembers.size());
  }


  // --- [트리거 2] 답변 생성 완료 시 (다른 가족에게 알림) ---
  // 답변 등록 시, 답변 작성자를 제외한 다른 가족 구성원에게 알림을 보냅니다. (AnswerService에서 호출)
  public void notifyAnswerCompleted(QuestionInstance questionInstance, Member responder) {
    log.info("[SYNC] 답변 완료 알림 처리 시작. QuestionId: {}", questionInstance.getId());
    // 알림 대상자 조회 (응답자 제외)
    List<Member> targetMembers = memberRepository.findAllByFamily_IdAndIdNot(responder.getFamily().getId(), responder.getId());
    List<UUID> targetIds = targetMembers.stream().map(Member::getId).toList();

    String title = "가족의 답변이 도착했어요! 👀";
    String body = String.format("%s님이 질문에 답변했습니다. 지금 확인해보세요!", responder.getFamilyRole().toString());
    sendNotificationToMemberIds(targetIds, title, body, NotificationTarget.from(questionInstance.getId(), NotificationType.ANSWER_COMPLETED));
    log.info("[SYNC] 답변 완료 알림 발송 완료. 대상 인원: {}", targetMembers.size());
  }


  // --- [트리거 3] 댓글 생성 시 (답변/댓글 대상자에게 알림) ---
  // 새 댓글 등록 시, 알림 대상 멤버에게 알림을 보냅니다. (CommentService에서 호출)
  public void notifyNewComment(Member targetMember, Member commenter, UUID targetId) {
    log.info("[SYNC] 새 댓글 알림 처리 시작. TargetMemberId: {}", targetMember.getId());
    String title = "새로운 댓글이 달렸어요! 💭";
    String body = String.format("%s님이 회원님의 글에 댓글을 남겼습니다.", commenter.getFamilyRole().toString());

    // 단일 멤버 ID에 대한 알림
    sendNotificationToMemberIds(List.of(targetMember.getId()), title, body, NotificationTarget.from(targetId, NotificationType.NEW_COMMENT));
    log.info("[SYNC] 새 댓글 알림 발송 완료. TargetMemberId: {}", targetMember.getId());
  }

  // --- [트리거 4] 토큰 재발급 또는 로그인 시 (보안 알림) ---
  // 보안 이벤트 발생 시, 해당 멤버에게 알림을 보냅니다. (AuthService에서 호출)
  public void notifySecurityEvent(UUID memberId, boolean isNewLocation) {
    // Member 객체 조회는 pushToken이나 familyRole을 가져오기 위한 목적
    Member targetMember = memberRepository.findById(memberId).orElse(null);

    if (targetMember == null) {
      log.warn("[SYNC] 보안 알림 대상 멤버 {}를 찾을 수 없습니다.", memberId);
      return;
    }

    String title = "새로운 위치에서 로그인되었습니다.";
    String body = "계정 보호를 위해 본인이 아닐 경우 카카오 계정 비밀번호를 즉시 변경해주세요.";

    sendNotificationToMemberIds(List.of(memberId), title, body, NotificationTarget.from(memberId, NotificationType.SECURITY_EVENT));
    log.info("[SYNC] 보안 알림 발송 완료. MemberId: {}", memberId);
  }


  // 멤버 ID 목록에 해당하는 모든 FCM 토큰 조회 후 푸시 발송 시도
  private void sendNotificationToMemberIds(List<UUID> memberIds, String title, String body, NotificationTarget target) {
    List<FcmToken> tokens = fcmTokenRepository.findAllByMemberIdIn(memberIds);

    if (tokens.isEmpty()) {
      log.warn("알림 대상 멤버 ID {}에 해당하는 FCM 토큰이 없습니다.", memberIds);
      return;
    }

    tokens.forEach(token -> {
      // 각 토큰에 대해 동기적으로 푸시 발송
      sendPushNotificationInternal(token.getToken(), title, body, target);
    });
  }


  // Firebase Admin SDK를 사용하여 단일 푸시 알림 발송 (동기)
  private void sendPushNotificationInternal(String pushToken, String title, String body, NotificationTarget target) {
    if (pushToken == null || pushToken.isBlank()) {
      log.warn("Push token is missing for notification. Skipping.");
      return;
    }
    try {
      // Data Payload 구성
      Message message = buildMessage(pushToken, title, body, target.toDataMap());
      // 동기 호출: Google API 응답이 올 때까지 현재 쓰레드 블로킹
      String response = FirebaseMessaging.getInstance().send(message);
      log.debug("FCM 전송 성공 (응답: {}) to {}", response, pushToken);
    } catch (Exception e) {
      log.error("FCM push failed for token {}: {}", pushToken, e.getMessage());
      // TODO: 토큰이 만료되었거나 유효하지 않은 경우 DB에서 해당 토큰을 삭제하는 로직 추가 필요
    }
  }
  // Message 객체 빌드
  private Message buildMessage(String token, String title, String body, Map<String, String> data) {
    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(body)
        .build();
    return Message.builder()
        .setToken(token)
        .setNotification(notification)
        .putAllData(data)
        .build();
  }
}