package com.onsikku.onsikku_back.domain.question.domain;


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

  @Column(name = "due_at")
  private LocalDateTime dueAt;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "read_at")
  private LocalDateTime readAt;

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
    this.state = AssignmentState.DELIVERED;
  }

  public void markAsRead() {
    this.readAt = LocalDateTime.now();
    if (this.state == AssignmentState.DELIVERED) {
      log.debug("Assignment has been marked as READ");
      this.state = AssignmentState.READ;
    }
    else {
      log.debug("Assignment state is {}, not changing to READ", this.state);
    }
  }

  public void markAsAnswered() {
    this.answeredAt = LocalDateTime.now();
    this.state = AssignmentState.ANSWERED;
  }

  public static QuestionAssignment assignTo(QuestionInstance questionInstance, Member member) {
    QuestionAssignment assignment = new QuestionAssignment();
    assignment.questionInstance = questionInstance;
    assignment.member = member;
    assignment.reminderCount = 0;
    assignment.state = AssignmentState.PENDING;
    return assignment;
  }
}