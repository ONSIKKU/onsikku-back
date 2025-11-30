package com.onsikku.onsikku_back.domain.question.dto;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

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
