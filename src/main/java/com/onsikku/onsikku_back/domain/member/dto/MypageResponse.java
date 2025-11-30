package com.onsikku.onsikku_back.domain.member.dto;


import com.onsikku.onsikku_back.domain.member.domain.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MypageResponse {
  private Member member;
  private Family family;
  private List<Member> familyMembers;

  public static MypageResponse from(Member member, List<Member> familyMembers) {
    return MypageResponse.builder()
        .member(member)
        .family(member.getFamily())
        .familyMembers(familyMembers)
        .build();
  }
}