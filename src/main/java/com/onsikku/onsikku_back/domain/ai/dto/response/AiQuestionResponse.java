package com.onsikku.onsikku_back.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)   // 알 수 없는 속성 무시
public class AiQuestionResponse {
  private UUID memberId;
  private String content;
  private int level;
  private int priority;
  private JsonNode metadata;
  private int deletedCount;
  private String context;   // familyReport용
}
