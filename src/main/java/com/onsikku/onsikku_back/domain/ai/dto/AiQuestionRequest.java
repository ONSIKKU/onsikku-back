package com.onsikku.onsikku_back.domain.ai.dto;

import com.onsikku.onsikku_back.domain.ai.domain.AnswerAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiQuestionRequest {

  private String content;
  private String language;
  private String tone;
  private String category;
  private List<String> tags;
  private boolean subjectRequired;
  private String mood;
  private AnswerAnalysis answerAnalysis;


  public static AiQuestionRequest defaultRequest() {
    return AiQuestionRequest.builder()
        .language("ko")
        .tone("따뜻한")
        .category("가족")
        .tags(List.of("감사", "일상"))
        .subjectRequired(false)
        .mood("차분한")
        .build();
  }
}
