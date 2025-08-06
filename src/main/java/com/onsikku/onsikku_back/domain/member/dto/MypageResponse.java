package com.onsikku.onsikku_back.domain.member.dto;


import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.domain.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MypageResponse {
  private UUID id;
  private UUID familyId;
  private String familyName;
  private String familyInvitationCode;
  @Enumerated(EnumType.STRING)
  private Role role;
  private String kakaoId;
  private String profileImageUrl;
  private String relation;

  public static MypageResponse from(Member member) {
    return MypageResponse.builder()
        .id(member.getId())
        .familyId(member.getFamily().getId())
        .familyName(member.getFamily().getFamilyName())
        .familyInvitationCode(member.getFamily().getInvitationCode())
        .role(member.getRole())
        .kakaoId(member.getKakaoId())
        .profileImageUrl(member.getProfileImageUrl())
        .relation(member.getRelation())
        .build();
  }
}