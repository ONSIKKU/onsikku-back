package com.onsikku.onsikku_back.domain.question.dto;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.answer.domain.Reaction;
import com.onsikku.onsikku_back.domain.answer.domain.ReactionType;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.MemberQuestion;
import com.onsikku.onsikku_back.domain.question.domain.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class QuestionDetails {
  private UUID memberQuestionId;
  private String content;
  private QuestionStatus questionStatus;
  private LocalDate sentDate;
  private Member member;
  private Answer answer;
  private List<Comment> comments;
  private ReactionType myReaction;
  private Long likeCount;
  private Long angryCount;
  private Long sadCount;
  private Long funnyCount;


  public static QuestionDetails from(MemberQuestion memberQuestion, Answer answer, List<Comment> comments, List<Reaction> reactions, UUID currentMemberId) {
    ReactionType myReaction = reactions.stream()
        .filter(r -> r.getMember().getId().equals(currentMemberId))
        .map(Reaction::getReactionType)
        .findFirst()
        .orElse(null);

    return QuestionDetails.builder()
        .memberQuestionId(memberQuestion.getId())
        .content(memberQuestion.getContent())
        .questionStatus(memberQuestion.getQuestionStatus())
        .sentDate(memberQuestion.getSentAt().toLocalDate())
        .member(memberQuestion.getMember())
        .answer(answer)
        .comments(comments)
        .myReaction(myReaction)
        .likeCount(reactions.stream().filter(r -> r.getReactionType() == ReactionType.LIKE).count())
        .angryCount(reactions.stream().filter(r -> r.getReactionType() == ReactionType.ANGRY).count())
        .sadCount(reactions.stream().filter(r -> r.getReactionType() == ReactionType.SAD).count())
        .funnyCount(reactions.stream().filter(r -> r.getReactionType() == ReactionType.FUNNY).count())
        .build();
  }

  public static QuestionDetails fromMemberQuestion(MemberQuestion memberQuestion) {
    return QuestionDetails.builder()
        .memberQuestionId(memberQuestion.getId())
        .content(memberQuestion.getContent())
        .questionStatus(memberQuestion.getQuestionStatus())
        .sentDate(memberQuestion.getSentAt().toLocalDate())
        .member(memberQuestion.getMember())
        .build();
  }
}
