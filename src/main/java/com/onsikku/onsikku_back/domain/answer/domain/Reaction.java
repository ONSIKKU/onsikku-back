package com.onsikku.onsikku_back.domain.answer.domain;


import com.onsikku.onsikku_back.domain.answer.dto.ReactionType;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reaction")
public class Reaction {
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

  @Enumerated(EnumType.STRING)
  @Column(name = "reaction_type", nullable = false)
  private ReactionType reactionType;

  public static Reaction createReaction(Answer answer, Member member, ReactionType reactionType) {
    return Reaction.builder()
        .answer(answer)
        .member(member)
        .reactionType(reactionType)
        .build();
  }
}
