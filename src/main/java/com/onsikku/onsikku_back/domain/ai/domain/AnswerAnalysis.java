package com.onsikku.onsikku_back.domain.ai.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.ai.dto.response.AnswerAnalysisResponse;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "answer_analysis",
    uniqueConstraints = @UniqueConstraint(name = "uq_answer_analysis_version", columnNames = {"answer_id","analysis_version"}),
    indexes = {
        @Index(name = "idx_answer_analysis_answer", columnList = "answer_id")
    })
public class AnswerAnalysis extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "answer_id", nullable = false)
  @JsonIgnore
  private Answer answer;

  @Type(JsonBinaryType.class)
  @Column(name = "analysis_parameters", columnDefinition = "jsonb")
  private JsonNode analysisParameters;

  @Column(name = "analysis_prompt", columnDefinition = "text")
  @JsonIgnore
  private String analysisPrompt;

  @Type(JsonBinaryType.class)
  @Column(name = "analysis_raw", columnDefinition = "jsonb")
  @JsonIgnore
  private JsonNode analysisRaw;

  @Column(name = "analysis_version")
  private String analysisVersion;

  @Column(name = "summary", columnDefinition = "text")
  private String summary;

  @Type(JsonBinaryType.class)
  @Column(name = "categories", columnDefinition = "jsonb")
  private JsonNode categories;

  @Type(JsonBinaryType.class)
  @Column(name = "scores", columnDefinition = "jsonb")
  private JsonNode scores;

  @Type(JsonBinaryType.class)
  @Column(name = "keywords", columnDefinition = "jsonb")
  private JsonNode keywords;

  public static AnswerAnalysis createFromAIResponse(Answer answer, AnswerAnalysisResponse response, JsonNode categories, JsonNode scores, JsonNode keywords) {
    return AnswerAnalysis.builder()
        .answer(answer)
        .analysisParameters(response.getAnalysisParameters())
        .analysisPrompt(response.getAnalysisPrompt())
        .analysisRaw(response.getAnalysisRaw())
        .analysisVersion(response.getAnalysisVersion())
        .summary(response.getSummary())
        .categories(categories)
        .scores(scores)
        .keywords(keywords)
        .build();
  }
}