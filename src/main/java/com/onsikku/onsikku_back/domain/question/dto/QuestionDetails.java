package com.onsikku.onsikku_back.domain.question.dto;


import com.onsikku.onsikku_back.domain.member.domain.*;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.domain.question.domain.enums.AssignmentState;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class QuestionDetails {
  private UUID questionAssignmentId;
  private UUID memberId;
  private FamilyRole familyRole;
  private String profileImageUrl;
  private Gender gender;
  private AssignmentState state;
  private LocalDateTime dueAt;
  private LocalDateTime sentAt;
  private LocalDateTime answeredAt;
  private LocalDateTime expiredAt;
  private UUID questionInstanceId;
  private String questionContent;

  public static QuestionDetails from(QuestionAssignment assignment) {
    return QuestionDetails.builder()
        .questionAssignmentId(assignment.getId())
        .memberId(assignment.getMember().getId())
        .familyRole(assignment.getMember().getFamilyRole())
        .profileImageUrl(assignment.getMember().getProfileImageUrl())
        .gender(assignment.getMember().getGender())
        .state(assignment.getState())
        .dueAt(assignment.getDueAt())
        .sentAt(assignment.getSentAt())
        .answeredAt(assignment.getAnsweredAt())
        .expiredAt(assignment.getExpiredAt())
        .questionInstanceId(assignment.getQuestionInstance().getId())
        .questionContent(assignment.getQuestionInstance().getContent())
        .build();
  }
}
