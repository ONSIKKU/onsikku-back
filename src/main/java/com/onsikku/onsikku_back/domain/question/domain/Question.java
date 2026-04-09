package com.onsikku.onsikku_back.domain.question.domain;

import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question")
public class Question extends BaseEntity {
  @Id
  @Column(length = 20)
  private String id;

  @Column(columnDefinition = "text", nullable = false)
  private String content;

  @Column(length = 20, nullable = false)
  private String category;

  @Column(nullable = false)
  private int level;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;
}
