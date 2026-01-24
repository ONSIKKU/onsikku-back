package com.onsikku.onsikku_back.domain.question.domain;

import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question")
public class Question extends BaseEntity {
  @Id
  @Column(length = 5)
  private String id;

  @Column(columnDefinition = "text", nullable = false)
  private String content;

  private String category;


  @Column(name = "archived_at")
  private LocalDateTime archivedAt;
}