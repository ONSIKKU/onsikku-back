package com.onsikku.onsikku_back.domain.ai.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class AnswerAnalysisResponse {
  private String analysisPrompt;     // "analysis_prompt" 와 매핑
  private JsonNode analysisParameters; // "analysis_parameters" 와 매핑
  private JsonNode analysisRaw;      // "analysis_raw" 와 매핑
  private String analysisVersion;    // "analysis_version" 와 매핑
  private String summary;
  private List<String> categories;
  private JsonNode scores;
  private List<String> keywords;
  private LocalDateTime createdAt;         // "created_at" 와 매핑
}