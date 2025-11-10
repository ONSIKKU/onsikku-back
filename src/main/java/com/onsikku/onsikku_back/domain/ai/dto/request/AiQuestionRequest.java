package com.onsikku.onsikku_back.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.onsikku.onsikku_back.domain.ai.domain.AnswerAnalysis;
import com.onsikku.onsikku_back.domain.question.domain.QuestionTemplate;
import com.onsikku.onsikku_back.domain.question.domain.enums.QuestionSource;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
@JsonInclude(Include.NON_NULL)
public class AiQuestionRequest {

  private String content;
  private String language;
  private String tone;
  private String category;
  private List<String> tags;
  private boolean subjectRequired;    // 주제에 관련 인물 포함 여부
  private String mood;
  private AnswerAnalysis answerAnalysis;

  public static AiQuestionRequest forFollowUp(String prevQuestion, String prevAnswer) {
    return AiQuestionRequest.builder()
        .content(prevQuestion)
        .build();
  }

  public static AiQuestionRequest fromTemplate(QuestionTemplate template) {
    return AiQuestionRequest.builder()
        .language(template.getLanguage())
        .tone(template.getTone())
        .category(template.getCategory())
        .tags(template.getTags())
        .subjectRequired(template.getSubjectRequired())
        .mood("차분한")
        .build();
  }

  public static AiQuestionRequest defaultRequest() {
    return AiQuestionRequest.builder()
        .content("일반 질문")
        .language("ko")
        .tone("따뜻한")
        .category("가족")
        .tags(List.of("감사", "일상"))
        .subjectRequired(false)
        .mood("차분한")
        .answerAnalysis(null)
        .build();
  }
}
