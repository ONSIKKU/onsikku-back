package com.onsikku.onsikku_back.domain.ai.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.ai.domain.AnswerAnalysis;
import lombok.Builder;


@Builder
public class AnswerAnalysisDetails {
  private String summary;
  private JsonNode categories;
  private JsonNode scores;
  private JsonNode keywords;

  public static AnswerAnalysisDetails fromAnswerAnalysis(AnswerAnalysis analysis) {
    return AnswerAnalysisDetails.builder()
        .summary(analysis.getSummary())
        .categories(analysis.getCategories())
        .scores(analysis.getScores())
        .keywords(analysis.getKeywords())
        .build();
  }
}
