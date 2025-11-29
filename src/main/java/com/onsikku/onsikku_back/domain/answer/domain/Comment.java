package com.onsikku.onsikku_back.domain.answer.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comment")
@ToString
public class Comment extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_instance_id", nullable = false)
  @JsonIgnore
  private QuestionInstance questionInstance;


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id", nullable = true)
  @Setter
  private Comment parent;

  @Column(name = "content", columnDefinition = "text", nullable = false)
  private String content;

  public void updateContent(@NotBlank(message = "내용을 입력해주세요.") String content) {
    this.content = content;
  }
}