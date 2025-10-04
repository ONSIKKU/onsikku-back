package com.onsikku.onsikku_back.domain.ai.domain;

import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "member_affinity",
    uniqueConstraints = @UniqueConstraint(name = "uq_affinity", columnNames = {"family_id","subject_member_id","target_member_id"}),
    indexes = {
        @Index(name = "idx_affinity_family_subject", columnList = "family_id,subject_member_id")
    })
public class MemberAffinity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "family_id", nullable = false)
  private Family family;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subject_member_id", nullable = false)
  private Member subject;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_member_id", nullable = false)
  private Member target;


  @Column(name = "affinity_score", nullable = false)
  private Float affinityScore; // 0..1


  @Column(name = "last_updated_at")
  private OffsetDateTime lastUpdatedAt;


  @PrePersist @PreUpdate
  void validate() {
    if (subject != null && target != null && Objects.equals(subject.getId(), target.getId())) {
      throw new IllegalArgumentException("subject_member_id and target_member_id must differ");
    }
    if (affinityScore != null && (affinityScore < 0f || affinityScore > 1f)) {
      throw new IllegalArgumentException("affinity_score must be within [0,1]");
    }
    this.lastUpdatedAt = OffsetDateTime.now();
  }
}