package com.onsikku.onsikku_back.domain.ai.dto.request;


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
}