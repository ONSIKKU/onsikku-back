package com.onsikku.onsikku_back.domain.question.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.enums.QuestionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    indexes = {
        //@Index(name = "idx_qa_family_sent_state", columnList = "family_id, sent_at DESC, state")
    })
public class MemberQuestion {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

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

  public void markAsSent(LocalDateTime dueAt) {
    this.sentAt = LocalDateTime.now();
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

  public void markAsAnswered() {
    this.answeredAt = LocalDateTime.now();
    this.questionStatus = QuestionStatus.ANSWERED;
  }

  public static MemberQuestion createAndAssignTo(Member member, Family family) {
    return MemberQuestion.builder()
        .family(family)
        .member(member)
        .reminderCount(0)
        .questionStatus(QuestionStatus.PENDING)
        .build();
  }
}