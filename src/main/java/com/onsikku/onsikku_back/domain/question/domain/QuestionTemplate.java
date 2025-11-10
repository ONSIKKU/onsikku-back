package com.onsikku.onsikku_back.domain.question.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.question.domain.enums.ReuseScope;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import com.vladmihalcea.hibernate.type.array.ListArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question_template")
public class QuestionTemplate extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "text", nullable = false)
  private String content;

  private String category;

  @JdbcTypeCode(SqlTypes.ARRAY) // SQL ARRAY 타입을 사용하도록 명시
  @Column(columnDefinition = "text[]")
  private List<String> tags; // e.g., ["daily","fun"]

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

  @PrePersist
  void prePersist() {
    if (isActive == null) isActive = true;
  }
}