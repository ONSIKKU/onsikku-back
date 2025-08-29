package com.onsikku.onsikku_back.domain.ai.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;


import java.time.*;
import java.util.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "member_profile")
public class MemberProfile extends BaseEntity {
  @Id
  @Column(name = "member_id", nullable = false)
  private UUID memberId; // 0..1 with Member


  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "member_id")
  private Member member;


  @Type(JsonBinaryType.class)
  @Column(columnDefinition = "jsonb")
  private JsonNode preferences;


  @Type(JsonBinaryType.class)
  @Column(name = "engagement_stats", columnDefinition = "jsonb")
  private JsonNode engagementStats;


  @Column(name = "last_ai_update_at")
  private OffsetDateTime lastAiUpdateAt;
}