package com.onsikku.onsikku_back.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.domain.question.domain.QuestionTemplate;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

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
  private boolean subjectRequired;
  private UUID subjectMemberId;
  private AnswerAnalysisDetails answerAnalysis;

  public static AiQuestionRequest forFollowUp(QuestionInstance instance, AnswerAnalysisDetails answerAnalysis) {
    return AiQuestionRequest.builder()
        .content(instance.getContent())
        .language(instance.getTemplate().getLanguage())
        .tone(instance.getTemplate().getTone())
        .category(instance.getTemplate().getCategory())
        .tags(instance.getTemplate().getTags())
        .subjectRequired(instance.getTemplate().getSubjectRequired())
        .subjectMemberId(instance.getSubject() != null ? instance.getSubject().getId() : null)
        .answerAnalysis(answerAnalysis)
        .build();
  }

  public static AiQuestionRequest fromTemplate(QuestionTemplate template) {
    return AiQuestionRequest.builder()
        .content(template.getContent())
        .language(template.getLanguage())
        .tone(template.getTone())
        .category(template.getCategory())
        .tags(template.getTags())
        .subjectRequired(template.getSubjectRequired())
        .build();
  }
}
