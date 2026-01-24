package com.onsikku.onsikku_back.domain.question.dto;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.MemberQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class QuestionDetails {
  private UUID memberQuestionId;
  private String content;
  private Member member;
  private Answer answer;
  private List<Comment> comments;


  public static QuestionDetails fromOnlyMemberQuestion(MemberQuestion memberQuestion) {
    return QuestionDetails.builder()
        .memberQuestionId(memberQuestion.getId())
        .content(memberQuestion.getContent())
        .member(memberQuestion.getMember())
        .build();
  }
  public static QuestionDetails from(MemberQuestion memberQuestion, Answer answer, List<Comment> comments) {
    return QuestionDetails.builder()
        .memberQuestionId(memberQuestion.getId())
        .content(memberQuestion.getContent())
        .member(memberQuestion.getMember())
        .answer(answer)
        .comments(comments)
        .build();
  }

  public static QuestionDetails fromMemberQuestion(MemberQuestion memberQuestion) {
    return QuestionDetails.builder()
        .memberQuestionId(memberQuestion.getId())
        .content(memberQuestion.getContent())
        .member(memberQuestion.getMember())
        .build();
  }
}
