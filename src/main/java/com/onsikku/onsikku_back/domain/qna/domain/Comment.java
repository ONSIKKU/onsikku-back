package com.onsikku.onsikku_back.domain.qna.domain;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments",
    indexes = {
        @Index(name = "idx_comments_answer", columnList = "answer_id"),
        @Index(name = "idx_comments_commenter", columnList = "commenter_member_id")
    })
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "answer_id", nullable = false)
  private Answer answer;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "commenter_member_id", nullable = false)
  private Member commenter;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id")
  private Comment parent;


  @Column(name = "content", columnDefinition = "text", nullable = false)
  private String content;


  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;


  @Column(name = "edited_at")
  private OffsetDateTime editedAt;


  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;


  @PrePersist
  void prePersist() { this.createdAt = OffsetDateTime.now(); }
}