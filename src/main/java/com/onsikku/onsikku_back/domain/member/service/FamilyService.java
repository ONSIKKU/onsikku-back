package com.onsikku.onsikku_back.domain.member.service;


import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.FamilyRepository;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.question.repository.QuestionAssignmentRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FamilyService {

  private final FamilyRepository familyRepository;
  private final MemberRepository memberRepository;
  private final QuestionAssignmentRepository questionAssignmentRepository;

  public Map<UUID, Integer> getMemberAssignedCounts(Family family) {
    List<Member> members = memberRepository.findAllByFamily_Id(family.getId());
    return members.stream()
        .collect(
            java.util.stream.Collectors.toMap(
                Member::getId,
                member -> questionAssignmentRepository.countByMemberIdAndSentAtAfter(
                    member.getId(),
                    LocalDateTime.now().minusDays(30)
                )
            )
        );
  }

  private Family validateFamilyMember(UUID familyId, Member member) {
    if (!familyId.equals(member.getFamily().getId())) {
      throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
    }
    return familyRepository.findById(familyId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.FAMILY_NOT_FOUND));
  }
}
