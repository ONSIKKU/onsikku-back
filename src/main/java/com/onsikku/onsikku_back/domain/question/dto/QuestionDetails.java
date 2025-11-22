package com.onsikku.onsikku_back.domain.question.dto;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.answer.dto.CommentResponse;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class QuestionDetails {
  private UUID questionInstanceId;
  private String questionContent;
  // TODO : 월별 조회시 가독성 안좋으므로 별도 DTO로 분리 고려
  private List<QuestionAssignment> questionAssignments; // 질문이 할당된 멤버들
  private List<AnswerResponse> answers;
  private List<Comment> comments;

  public static QuestionDetails from(QuestionInstance questionInstance, List<QuestionAssignment> questionAssignments, List<AnswerResponse> answers, List<Comment> comments) {
    return QuestionDetails.builder()
        .questionInstanceId(questionInstance.getId())
        .questionContent(questionInstance.getContent())
        .questionAssignments(questionAssignments)
        .answers(answers)
        .comments(comments)
        .build();
  }
}
