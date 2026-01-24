package com.onsikku.onsikku_back.domain.ai.dto.request;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AnswerAnalysisRequest {
  private String answerText;
  private String language;
  private String questionContent;
  private String questionCategory;
  private List<String> questionTags;
  private String questionTone;
  private UUID userId;

  public static AnswerAnalysisRequest createFromAnswerAndQuestionInstance(Answer answer, QuestionInstance questionInstance) {
    if (questionInstance.getTemplate() == null) {
      return AnswerAnalysisRequest.builder()
          .answerText(answer.extractTextContent())
          .language("ko")
          // TODO : 변경 필요
          .questionContent(questionInstance.getContent())
          .questionCategory("가족")
          .questionTags(List.of("감사", "일상"))
          .questionTone("따뜻한")
          .userId(answer.getMember().getId())
          .build();
    }
    return AnswerAnalysisRequest.builder()
        .answerText(answer.extractTextContent())
        .language(questionInstance.getTemplate().getLanguage())
        .questionContent(questionInstance.getContent())
        .questionCategory(questionInstance.getTemplate().getCategory())
        .questionTags(questionInstance.getTemplate().getTags())
        .questionTone(questionInstance.getTemplate().getTone())
        .userId(answer.getMember().getId())
        .build();
  }
}