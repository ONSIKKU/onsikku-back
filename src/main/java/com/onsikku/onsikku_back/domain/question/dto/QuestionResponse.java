package com.onsikku.onsikku_back.domain.question.dto;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class QuestionResponse {
  private List<QuestionDetails> questionDetailsList;
  private QuestionDetails questionDetails;
  private int totalQuestions;
  private int answeredQuestions;
  private int totalReactions;
  private List<Member> familyMembers;
}
