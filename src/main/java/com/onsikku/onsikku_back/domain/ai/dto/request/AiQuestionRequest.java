package com.onsikku.onsikku_back.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.onsikku.onsikku_back.domain.question.domain.Question;
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
}
