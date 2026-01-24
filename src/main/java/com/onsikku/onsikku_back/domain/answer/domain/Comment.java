package com.onsikku.onsikku_back.domain.answer.domain;

import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comment")
public class Comment extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "answer_id", nullable = false)
  private Answer answer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  @Setter
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id", nullable = true)
  @Setter
  @OnDelete(action = OnDeleteAction.SET_NULL)    // 삭제시 자식 댓글은 자동으로 null 처리됨
  private Comment parentComment;

  @Column(name = "content", columnDefinition = "text", nullable = false)
  private String content;

  @Column(name = "deleted_at", nullable = true)
  private LocalDateTime deletedAt;

  public void updateContent(@NotBlank(message = "내용을 입력해주세요.") String content) {
    this.content = content;
  }
}