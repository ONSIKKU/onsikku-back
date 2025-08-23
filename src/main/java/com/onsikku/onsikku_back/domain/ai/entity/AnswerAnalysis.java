package com.onsikku.onsikku_back.domain.ai.entity;
import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.qna.domain.Answer;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
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
public class AnswerAnalysis {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "answer_id", nullable = false)
  private Answer answer;


  @Column(name = "analysis_model")
  private String analysisModel;


  @Type(JsonBinaryType.class)
  @Column(name = "analysis_parameters", columnDefinition = "jsonb")
  private JsonNode analysisParameters;


  @Column(name = "analysis_prompt", columnDefinition = "text")
  private String analysisPrompt;


  @Type(JsonBinaryType.class)
  @Column(name = "analysis_raw", columnDefinition = "jsonb")
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


  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;


  @PrePersist
  void prePersist() { this.createdAt = OffsetDateTime.now(); }
}