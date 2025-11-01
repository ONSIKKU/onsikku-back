package com.onsikku.onsikku_back.domain.question.domain;


import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.ai.dto.response.AiQuestionResponse;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.enums.GeneratedBy;
import com.onsikku.onsikku_back.domain.question.domain.enums.InstanceStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question_instance",
    uniqueConstraints = @UniqueConstraint(name = "uq_instance_per_day", columnNames = {"family_id","planned_date"}),
    indexes = {
        @Index(name = "idx_instance_family_planned", columnList = "family_id,planned_date"),
        @Index(name = "idx_instance_template", columnList = "template_id"),
        @Index(name = "idx_instance_subject", columnList = "subject_member_id")
    })
public class QuestionInstance {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "family_id", nullable = false)
  private Family family;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", nullable = true)
  @Setter
  private QuestionTemplate template;  // optional

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subject_member_id", nullable = true)
  private Member subject;             // 누군가한테 특화돼있는 질문일 경우 optional

  @Column(columnDefinition = "text", nullable = false)
  private String content;

  @Column(name = "planned_date", nullable = false)
  private LocalDate plannedDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InstanceStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "generated_by")
  private GeneratedBy generatedBy; // nullable

  @Column(name = "generation_model")
  private String generationModel;

  @Type(JsonBinaryType.class)
  @Column(name = "generation_parameters", columnDefinition = "jsonb")
  private JsonNode generationParameters;

  @Column(name = "generation_prompt", columnDefinition = "text")
  private String generationPrompt;

  @Type(JsonBinaryType.class)
  @Column(name = "generation_metadata", columnDefinition = "jsonb")
  private JsonNode generationMetadata;

  @Column(name = "generation_confidence")
  private Float generationConfidence;

  @Column(name = "generated_at")
  private LocalDateTime generatedAt;

  @Column(name = "scheduled_at")
  private LocalDateTime scheduledAt;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "canceled_at")
  private LocalDateTime canceledAt;

  public static QuestionInstance generateByAI(AiQuestionResponse response, Family family) {
    GeneratedBy generatedBy = null;
    InstanceStatus status = null;
    try {
      if (response.getGeneratedBy() != null) {
        generatedBy = GeneratedBy.valueOf(response.getGeneratedBy().toUpperCase());
      }
      if (response.getStatus() != null) {
        status = InstanceStatus.valueOf(response.getStatus().toUpperCase());
      }
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid ENUM value from AI response: " +
          "generatedBy : " + response.getGeneratedBy() + ", status : " + response.getStatus(), e);
    }

    return QuestionInstance.builder()
        .family(family)
        .content(response.getContent())
        .plannedDate(response.getPlannedDate())
        .generatedAt(response.getGeneratedAt())
        .generationMetadata(response.getGenerationMetadata())
        .generationModel(response.getGenerationModel())
        .generationPrompt(response.getGenerationPrompt())
        .generationParameters(response.getGenerationParameters())
        .generationConfidence(response.getGenerationConfidence())
        .generatedBy(generatedBy)
        .status(status)
        .build();
  }
}