package com.onsikku.onsikku_back.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Builder
@ToString
@JsonInclude(Include.NON_NULL)
public class AiQuestionRequest {
  private UUID familyId;
  private UUID memberId;
  private String roleLabel;
  private String baseQuestion;
  private String baseAnswer;
  private String answeredAt;

  public static AiQuestionRequest of(Member member, String baseQuestion, String baseAnswer, String answeredAt) {
    return AiQuestionRequest.builder()
        .familyId(member.getFamily().getId())
        .memberId(member.getId())
        .roleLabel(member.getFamilyRole().name())
        .baseQuestion(baseQuestion)
        .baseAnswer(baseAnswer)
        .answeredAt(answeredAt)
        .build();
  }
}
