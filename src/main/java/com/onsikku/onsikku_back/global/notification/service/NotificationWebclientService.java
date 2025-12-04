package com.onsikku.onsikku_back.global.notification.service;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationWebclientService {

  // --- [트리거 1] 새로운 질문 할당 시 (스케줄러 호출) ---
  /**
   * 새로운 질문이 생성 및 할당되었을 때, 모든 가족 구성원에게 알림을 보냅니다.
   * 이 메서드는 QuestionService (스케줄러 로직)에서 호출됩니다.
   */
  @Async("notificationTaskExecutor")
  public void notifyNewQuestionAssigned(QuestionInstance questionInstance, List<Member> assignedMembers) {
    log.info("[ASYNC] 새로운 질문 할당 알림 처리 시작. QuestionId: {}", questionInstance.getId());

    String title = "오늘의 가족 질문이 도착했어요! 💬";
    String body = "지금 바로 답변하고 가족에게 나의 생각을 알려주세요.";

    assignedMembers.forEach(target -> {
      //fcmClientService.sendPushNotification(target.getPushToken(), title, body, NotificationTarget.from(questionInstance.getId(), NotificationType.NEW_QUESTION_ASSIGNED));
    });
    log.info("[ASYNC] 새로운 질문 할당 알림 발송 완료. 대상 인원: {}", assignedMembers.size());
  }


  // --- [트리거 2] 답변 생성 완료 시 (다른 가족에게 알림) ---
  /**
   * 답변 등록 시, 답변 작성자를 제외한 다른 가족 구성원에게 알림을 보냅니다.
   * 이 메서드는 AnswerService에서 호출됩니다.
   * @param questionInstance 질문 인스턴스
   * @param responder 답변을 등록한 멤버
   */
  @Async("notificationTaskExecutor")
  public void notifyAnswerCompleted(QuestionInstance questionInstance, Member responder) {
    log.info("[ASYNC] 답변 완료 알림 처리 시작. QuestionId: {}", questionInstance.getId());

    // 1. 알림 대상자 조회: 응답자를 제외한 모든 가족 구성원
    List<Member> targetMembers = null; //memberQueryService.findAllFamilyMembersExcluding(responder.getFamily().getId(), responder.getId());

    // 2. 알림 메시지 구성
    String title = "가족의 답변이 도착했어요! 👀";
    String body = String.format("%s님이 질문에 답변했습니다. 지금 확인해보세요!", responder.getFamilyRole().toString());

    // 3. 알림 발송
    targetMembers.forEach(target -> {
      //fcmClientService.sendPushNotification(target.getPushToken(), title, body, NotificationTarget.from(questionInstance.getId(), NotificationType.ANSWER_COMPLETED));
    });
    log.info("[ASYNC] 답변 완료 알림 발송 완료. 대상 인원: {}", targetMembers.size());
  }


  // --- [트리거 3] 댓글 생성 시 (답변/댓글 대상자에게 알림) ---
  /**
   * 새 댓글 등록 시, 알림 대상 멤버에게 알림을 보냅니다.
   * 이 메서드는 CommentService에서 호출됩니다.
   * @param targetMember 알림을 받을 멤버 (댓글의 주체)
   * @param commenter 댓글 작성자
   * @param targetId 댓글이 달린 대상 ID (Instance ID, Answer ID, Comment ID 등)
   */
  @Async("notificationTaskExecutor")
  public void notifyNewComment(Member targetMember, Member commenter, UUID targetId) {
    log.info("[ASYNC] 새 댓글 알림 처리 시작. TargetMemberId: {}", targetMember.getId());

    String title = "새로운 댓글이 달렸어요! 💭";
    String body = String.format("%s님이 회원님의 글에 댓글을 남겼습니다.", commenter.getFamilyRole().toString());

    //fcmClientService.sendPushNotification(targetMember.getPushToken(), title, body, NotificationTarget.from(targetId, NotificationType.NEW_COMMENT));
    log.info("[ASYNC] 새 댓글 알림 발송 완료. TargetMemberId: {}", targetMember.getId());
  }

  // --- [트리거 4] 토큰 재발급 또는 로그인 시 (보안 알림) ---
  /**
   * 보안 이벤트 (로그인/재발급) 발생 시, 해당 멤버에게 알림을 보냅니다.
   * 이 메서드는 AuthService에서 호출됩니다.
   * @param memberId 대상 멤버 ID
   * @param isNewLocation 새로운 위치에서의 접근 여부
   */
  @Async("notificationTaskExecutor")
  public void notifySecurityEvent(UUID memberId, boolean isNewLocation) {
    // 실제 멤버 객체를 DB에서 조회 (pushToken 및 familyRole 획득 목적)
    Member targetMember = null; //memberQueryService.findMemberById(memberId);

    if (targetMember == null) {
      log.warn("[ASYNC] 보안 알림 대상 멤버 {}를 찾을 수 없습니다.", memberId);
      return;
    }

    String title = isNewLocation ? "🚨 보안 알림: 새로운 위치에서 로그인되었습니다" : "세션 갱신 완료 (Refresh Token 사용)";
    String body = isNewLocation ? "계정 보호를 위해 본인이 아닐 경우 즉시 비밀번호를 변경해주세요." : "정상적인 활동입니다. 감사합니다.";

    //fcmClientService.sendPushNotification(targetMember.getPushToken(), title, body, NotificationTarget.from(memberId, NotificationType.SECURITY_EVENT));
    log.info("[ASYNC] 보안 알림 발송 완료. MemberId: {}", memberId);
  }
}