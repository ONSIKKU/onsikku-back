package com.onsikku.onsikku_back.domain.member.dto;


import com.onsikku.onsikku_back.domain.member.domain.FamilyRole;
import com.onsikku.onsikku_back.domain.member.domain.Gender;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.domain.Role;
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
  private String familyName;
  private String familyInvitationCode;
  private Role role;
  private String profileImageUrl;
  private FamilyRole familyRole;
  private LocalDate birthDate;
  private Gender gender;
  private boolean isAlarmEnabled;

  public static MypageResponse from(Member member) {
    return MypageResponse.builder()
        .familyName(member.getFamily().getFamilyName())
        .familyInvitationCode(member.getFamily().getInvitationCode())
        .role(member.getRole())
        .profileImageUrl(member.getProfileImageUrl())
        .familyRole(member.getFamilyRole())
        .birthDate(member.getBirthDate())
        .gender(member.getGender())
        .isAlarmEnabled(member.isAlarmEnabled())
        .build();
  }
}