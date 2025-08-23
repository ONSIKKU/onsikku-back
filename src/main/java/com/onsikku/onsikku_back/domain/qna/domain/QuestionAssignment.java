package com.onsikku.onsikku_back.domain.qna.domain;


import com.onsikku.onsikku_back.domain.ai.entity.QuestionInstance;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
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
  @JoinColumn(name = "recipient_member_id", nullable = false)
  private Member recipient;


  @Column(name = "due_at")
  private OffsetDateTime dueAt;


  @Column(name = "sent_at")
  private OffsetDateTime sentAt;


  @Column(name = "read_at")
  private OffsetDateTime readAt;


  @Column(name = "answered_at")
  private OffsetDateTime answeredAt;


  @Column(name = "expired_at")
  private OffsetDateTime expiredAt;


  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false)
  private AssignmentState state;


  @Column(name = "reminder_count", nullable = false)
  private Integer reminderCount;


  @Column(name = "last_reminded_at")
  private OffsetDateTime lastRemindedAt;


  @PrePersist
  void prePersist() {
    if (state == null) state = AssignmentState.PENDING;
    if (reminderCount == null) reminderCount = 0;
  }
}