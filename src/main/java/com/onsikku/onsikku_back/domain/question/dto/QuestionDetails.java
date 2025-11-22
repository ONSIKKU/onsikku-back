package com.onsikku.onsikku_back.domain.question.dto;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.member.domain.Member;
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
  private List<Member> assignedMembers; // 질문이 할당된 멤버들
  private List<Answer> answers;
  private List<Comment> comments;

  public static QuestionDetails from(QuestionInstance questionInstance, List<Member> members, List<Answer> answers, List<Comment> comments) {
    return QuestionDetails.builder()
        .questionInstanceId(questionInstance.getId())
        .questionContent(questionInstance.getContent())
        .assignedMembers(members)
        .answers(answers)
        .comments(comments)
        .build();
  }
}
