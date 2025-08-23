package com.onsikku.onsikku_back.domain.qna.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.member.domain.Family;
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
@Table(name = "question_template",
    indexes = {
        @Index(name = "idx_template_owner_family", columnList = "owner_family_id")
    })
public class QuestionTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_family_id")
  private Family ownerFamily; // nullable for GLOBAL


  @Column(columnDefinition = "text", nullable = false)
  private String content;


  private String category;


  @Type(JsonBinaryType.class)
  @Column(columnDefinition = "jsonb")
  private JsonNode tags; // e.g., ["daily","fun"]


  @Column(name = "subject_required")
  private Boolean subjectRequired;


  @Enumerated(EnumType.STRING)
  @Column(name = "reuse_scope")
  private ReuseScope reuseScope; // may be null


  @Column(name = "cooldown_days")
  private Integer cooldownDays;


  private String language;


  private String tone;


  @Column(name = "is_active")
  private Boolean isActive;


  @Column(name = "archived_at")
  private OffsetDateTime archivedAt;


  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;


  @PrePersist
  void prePersist() {
    if (isActive == null) isActive = true;
    this.createdAt = OffsetDateTime.now();
  }
}