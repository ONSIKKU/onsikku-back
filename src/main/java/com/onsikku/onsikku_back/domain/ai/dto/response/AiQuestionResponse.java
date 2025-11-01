package com.onsikku.onsikku_back.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.question.domain.enums.QuestionSource;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiQuestionResponse {

  @Setter
  private QuestionSource source;  // 질문 출처, setter로 설정 가능
  @Setter
  private UUID usedTemplateId; // 사용된 템플릿 ID, setter로 설정 가능

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
