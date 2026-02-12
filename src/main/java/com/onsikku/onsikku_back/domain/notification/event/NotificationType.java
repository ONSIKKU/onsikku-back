package com.onsikku.onsikku_back.domain.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

  // --- [주인공 전용] 오늘의 질문 ---
  // args: []
  TODAY_TARGET_MEMBER("오늘의 주인공은 바로 나! ✨", "오늘의 질문이 도착했어요. 시간 날 때 천천히 들려주세요."),

  // --- [가족들에게] (선택사항: 가족들에게 누가 주인공인지 미리 알려줄 때 필요) ---
  // "오늘 아빠가 주인공이래! 답변 기다려보자"
  // args: [주인공 이름]
  TODAY_TARGET_MEMBER_ANNOUNCED("오늘의 주인공은 %s님! 🌟", "어떤 답변을 남기실까요? 식구들이 기다리고 있어요."),

  // --- [가족 전용] 주인공 답변 완료 (열람 가능) ---
  // args: [주인공 이름]
  ANSWER_ADDED("오늘의 온식구 이야기가 도착했어요! 🔓", "오늘의 주인공 %s님이 답변을 남겼습니다. 지금 확인해보세요!"),

  // --- [가족 전용] 노크 (독촉) ---
  // args: []
  KNOCK_KNOCK("똑똑, 기다리고 있어요 👀", "식구들이 주인공님의 이야기를 궁금해해요."),

  // --- 소통 활성화 ---
  // args: [반응 남긴 사람 이름]
  REACTION_ADDED("%s님이 내 답변에 공감했어요", "가족의 따뜻한 마음을 확인해보세요! ❤️"),

  // args: [댓글 쓴 사람 이름, 댓글 내용 요약]
  COMMENT_ADDED("%s님이 대화에 참여했어요", "\"%s...\" 어떤 이야기를 남겼을까요?"),

  // --- 가족 관리 ---
  // args: [새 멤버 이름]
  MEMBER_JOINED("우리 식구가 더 늘어났어요!", "새로 합류한 %s님을 반갑게 맞이해 주세요! 👋"),

  // args: []
  WEEKLY_REPORT("우리 가족의 한 주가 소중하게 담겼어요", "함께 만든 기록들, 리포트로 가볍게 살펴보세요. 📋"),

  // args: [공지 제목]
  SYSTEM_NOTICE("온식구가 알려드려요!", "%s - 새로운 소식을 확인해 보세요!"),

  ;
  private final String title;
  private final String body;
}
