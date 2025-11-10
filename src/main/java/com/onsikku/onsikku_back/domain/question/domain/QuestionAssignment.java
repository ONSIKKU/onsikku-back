package com.onsikku.onsikku_back.domain.question.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.enums.AssignmentState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Slf4j
@Table(name = "question_assignment",
    indexes = {
        @Index(name = "idx_qa_family_sent_state", columnList = "family_id, sent_at DESC, state")
    })
public class QuestionAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_instance_id", nullable = false)
  private QuestionInstance questionInstance;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "family_id", nullable = false)
  @JsonIgnore
  private Family family;

  @Column(name = "due_at")
  private LocalDateTime dueAt;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  //@Column(name = "read_at")
  //private LocalDateTime readAt;

  @Column(name = "answered_at")
  private LocalDateTime answeredAt;

  @Column(name = "expired_at")
  private LocalDateTime expiredAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false)
  private AssignmentState state;

  @Column(name = "reminder_count", nullable = false)
  private Integer reminderCount;

  @Column(name = "last_reminded_at")
  private LocalDateTime lastRemindedAt;

  public void markAsSent(LocalDateTime dueAt) {
    this.sentAt = LocalDateTime.now();
    this.dueAt = dueAt;
    this.state = AssignmentState.SENT;
  }

  public void markAsExpired() {
    this.expiredAt = LocalDateTime.now();
    this.state = AssignmentState.EXPIRED;
  }

  public void markAsReminded() {
    this.reminderCount += 1;
    this.lastRemindedAt = LocalDateTime.now();
  }

  // TODO : 추후 필요 시 활성화
  /*public void markAsRead() {
    this.readAt = LocalDateTime.now();
    if (this.state == AssignmentState.DELIVERED) {
      log.debug("Assignment has been marked as READ");
      this.state = AssignmentState.READ;
    }
    else {
      log.debug("Assignment state is {}, not changing to READ", this.state);
    }
  }*/

  public void markAsAnswered() {
    this.answeredAt = LocalDateTime.now();
    this.state = AssignmentState.ANSWERED;
  }

  public static QuestionAssignment createAndAssignTo(QuestionInstance questionInstance, Member member, Family family) {
    return QuestionAssignment.builder()
        .questionInstance(questionInstance)
        .family(family)
        .member(member)
        .reminderCount(0)
        .state(AssignmentState.PENDING)
        .build();
  }
}