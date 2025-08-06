package com.onsikku.onsikku_back.domain.auth.service;

import com.onsikku.onsikku_back.domain.auth.domain.FamilyJoinMode;
import com.onsikku.onsikku_back.domain.auth.dto.KakaoLoginResponse;
import com.onsikku.onsikku_back.domain.auth.dto.KakaoMemberInfo;
import com.onsikku.onsikku_back.domain.auth.dto.KakaoSignupRequest;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.domain.Role;
import com.onsikku.onsikku_back.domain.member.repository.FamilyRepository;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.member.util.InvitationCodeGenerator;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.jwt.JwtProvider;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final KakaoOAuth2Service kakaoOAuth2Service;
  private final RegistrationTokenService registrationTokenService;
  private final MemberRepository memberRepository;
  private final FamilyRepository familyRepository;
  private final JwtProvider jwtProvider;

  @Transactional
  public KakaoLoginResponse kakaoLoginWithCode(String code) {
    // 카카오 OAuth2 인증 코드로부터 사용자 정보를 가져옵니다.
    KakaoMemberInfo memberInfo = kakaoOAuth2Service.getKakaoMemberInfoFromCode(code);

    // 사용자 정보를 기반으로 회원 정보를 조회합니다.
    Optional<Member> existing = memberRepository.findByKakaoId(memberInfo.kakaoId());

    // 이미 등록된 회원인 경우 JWT 토큰을 생성하여 반환합니다.
    if (existing.isPresent()) {
      return KakaoLoginResponse.builder()
          .accessToken(jwtProvider.generateTokenFromMember(existing.get()))
          .isRegistered(true)
          .build();
    }

    // 등록되지 않은 회원인 경우, 등록 토큰을 생성하여 반환합니다.
    return KakaoLoginResponse.builder()
        .registrationToken(registrationTokenService.save(memberInfo))
        .isRegistered(false)
        .build();
  }

  @Transactional
  public KakaoLoginResponse register(KakaoSignupRequest request) {
    // 요청에서 등록 토큰을 사용하여 카카오 회원 정보를 가져옵니다.
    KakaoMemberInfo memberInfo = registrationTokenService.get(request.registrationToken());

    // 카카오 ID로 이미 등록된 회원이 있는지 확인합니다.
    if (memberRepository.existsByKakaoId(memberInfo.kakaoId())) {
      throw new BaseException(BaseResponseStatus.ALREADY_REGISTERED);
    }

    // 가족 모드에 따라 가족을 생성하거나 조회합니다.
    Family family = getOrCreateFamily(request);

    // 새로운 회원 정보를 생성합니다.
    Member member = Member.builder()
        .kakaoId(memberInfo.kakaoId())
        .relation(request.relation())
        .profileImageUrl(request.profileImageUrl())
        .family(family)
        .role(Role.MEMBER)
        .build();

    // 회원 정보를 저장하고, 등록 토큰을 삭제합니다.
    memberRepository.save(member);
    registrationTokenService.delete(request.registrationToken());

    return KakaoLoginResponse.builder()
        .isRegistered(true)
        .accessToken(jwtProvider.generateTokenFromMember(member))
        .build();
  }

  // ------------------------- private 메소드 -------------------------

  private Family getOrCreateFamily(KakaoSignupRequest request) {
    // 가족 생성 모드인 경우, 새로운 가족을 생성합니다.
    if (request.familyMode().equals(FamilyJoinMode.CREATE.toString())) {
      return familyRepository.save(
          Family.builder()
              .familyName(request.familyName())
              .invitationCode(InvitationCodeGenerator.generate())
              .build()
      );
    }
    // 가족 초대 모드인 경우, 가족 이름으로 가족을 조회합니다.
    else if (request.familyMode().equals(FamilyJoinMode.JOIN.toString())) {
      return familyRepository.findByInvitationCode(request.familyInvitationCode())
          .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_FAMILY_INVITATION_CODE));
    } else {
      throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MODE);
    }
  }
}