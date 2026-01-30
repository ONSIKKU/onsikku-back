package com.onsikku.onsikku_back.domain.question.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.ai.dto.response.AiQuestionResponse;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "member_question",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_member_question_template",
            columnNames = {"member_id", "question_id"} // 복합 유니크 제약 조건
            // TODO : 유저가 셔플한 질문을 다시 줄 수 있어야 한다면, 복합 유니크에 question_status 포함 고려
        )
    },
    indexes = {
        @Index(name = "idx_member_id", columnList = "member_id")
    })
public class MemberQuestion extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY) // optional = true (템플릿 없는 질문 가능)
  @JoinColumn(name = "question_id", nullable = true)
  @JsonIgnore                       // 직렬화에서 제외
  private Question question;        // 질문 템플릿 참조

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "family_id", nullable = false)
  @JsonIgnore
  private Family family;

  private String content;

  private int level;      // 난이도 1~4 (평일에는 1~2, 주말에는 3~4)

  private int priority;   // 우선순위 (높을수록 우선순위 높음)

  @Enumerated(EnumType.STRING)
  @Column(name = "question_status", nullable = false)
  private QuestionStatus questionStatus;

  private int shuffleCount; // 질문 셔플 횟수

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private JsonNode metadata; // 추가 메타데이터 저장용

  @Column(name = "answered_at")
  private LocalDateTime answeredAt;

  @Column(name = "due_at")
  private LocalDateTime dueAt;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Column(name = "expired_at")
  private LocalDateTime expiredAt;

  @Column(name = "reminder_count", nullable = false)
  private Integer reminderCount;

  @Column(name = "last_reminded_at")
  private LocalDateTime lastRemindedAt;

  public void markAsSent(LocalDateTime scheduledAt, LocalDateTime dueAt) {
    this.sentAt = scheduledAt;    // 조회 시 sentAt <= now()
    this.dueAt = dueAt;
    this.questionStatus = QuestionStatus.SENT;
  }

  public void markAsExpired() {
    this.expiredAt = LocalDateTime.now();
    this.questionStatus = QuestionStatus.EXPIRED;
  }

  public void markAsReminded() {
    this.reminderCount += 1;
    this.lastRemindedAt = LocalDateTime.now();
  }

  public void markAsRead() {
    this.readAt = LocalDateTime.now();
    if (this.questionStatus == QuestionStatus.SENT) {
      this.questionStatus = QuestionStatus.READ;
    }
  }

  public boolean isAnswered() {
    if(this.questionStatus == QuestionStatus.ANSWERED) {
      return true;
    }
    return false;
  }

  public void markAsAnswered() {
    this.answeredAt = LocalDateTime.now();
    this.questionStatus = QuestionStatus.ANSWERED;
  }

  public static MemberQuestion createMemberQuestionFromQuestion(Member member, Family family, Question question) {
    return MemberQuestion.builder()
        .member(member)
        .family(family)
        .question(question)
        .content(question.getContent())
        .level(question.getLevel())
        .priority(1)        // 일반 템플릿이므로 우선순위 1
        .questionStatus(QuestionStatus.PENDING)
        .reminderCount(0)
        .build();
  }

  public static MemberQuestion createMemberQuestionFromAiResponse(Member member, AiQuestionResponse response) {
    return MemberQuestion.builder()
        .member(member)
        .family(member.getFamily())
        .content(response.getContent())
        .level(response.getLevel())
        .priority(response.getPriority())
        .questionStatus(QuestionStatus.PENDING)
        .metadata(response.getMetadata())
        .reminderCount(0)
        .build();
  }
}