package com.onsikku.onsikku_back.domain.answer.domain;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comment",
    indexes = {
        @Index(name = "idx_comments_answer", columnList = "answer_id"),
        @Index(name = "idx_comments_commenter", columnList = "commenter_member_id")
    })
public class Comment extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_instance_id", nullable = false)
  private QuestionInstance questionInstance;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "commenter_member_id", nullable = false)
  private Member commenter;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id")
  private Comment parent;

  @Column(name = "content", columnDefinition = "text", nullable = false)
  private String content;
}