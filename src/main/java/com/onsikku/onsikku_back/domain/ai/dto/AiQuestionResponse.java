package com.onsikku.onsikku_back.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@ToString
public class AiQuestionResponse {

  private String content;
  @JsonProperty("planned_date")
  private LocalDate plannedDate;
  private String status;
  @JsonProperty("generated_by")
  private String generatedBy;
  @JsonProperty("generation_model")
  private String generationModel;
  @JsonProperty("generation_parameters")
  private JsonNode generationParameters; // {}는 특정 타입이 없으므로 Object 또는 Map<String, Object>로 받음
  @JsonProperty("generation_prompt")
  private String generationPrompt;
  @JsonProperty("generation_metadata")
  private JsonNode generationMetadata;
  @JsonProperty("generation_confidence")
  private float generationConfidence;
  @JsonProperty("generated_at")
  private LocalDateTime generatedAt;
}
