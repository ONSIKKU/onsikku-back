package com.onsikku.onsikku_back.domain.question.domain;


import com.onsikku.onsikku_back.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question_assignment",
    uniqueConstraints = @UniqueConstraint(name = "uq_assignment", columnNames = {"instance_id","recipient_member_id"}),
    indexes = {
        @Index(name = "idx_assignment_recipient_state", columnList = "recipient_member_id,state"),
        @Index(name = "idx_assignment_instance", columnList = "instance_id")
    })
public class QuestionAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "instance_id", nullable = false)
  private QuestionInstance instance;


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


  @PrePersist
  void prePersist() {
    if (state == null) state = AssignmentState.PENDING;
    if (reminderCount == null) reminderCount = 0;
  }
}