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
  private int totalQuestionCount;             // 월별 조회
  private int answeredQuestionCount;          // 월별 조회
  private int totalReactionCount;             // 월별 조회
  private List<Member> familyMembers;         // 메인 화면
}
