package com.onsikku.onsikku_back.domain.member.dto;


import com.onsikku.onsikku_back.domain.member.domain.FamilyRole;
import com.onsikku.onsikku_back.domain.member.domain.Gender;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.domain.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
  private FamilyRole familyRole;
  private LocalDate birthDate;
  private Gender gender;

  public static MypageResponse from(Member member) {
    return MypageResponse.builder()
        .id(member.getId())
        .familyId(member.getFamily().getId())
        .familyName(member.getFamily().getFamilyName())
        .familyInvitationCode(member.getFamily().getInvitationCode())
        .role(member.getRole())
        .kakaoId(member.getKakaoId())
        .profileImageUrl(member.getProfileImageUrl())
        .familyRole(member.getFamilyRole())
        .birthDate(member.getBirthDate())
        .gender(member.getGender())
        .build();
  }
}