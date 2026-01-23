package com.onsikku.onsikku_back.global.auth.service;

import com.onsikku.onsikku_back.domain.question.service.QuestionService;
import com.onsikku.onsikku_back.global.auth.domain.FamilyMode;
import com.onsikku.onsikku_back.global.auth.dto.AuthResponse;
import com.onsikku.onsikku_back.global.auth.dto.AuthTestRequest;
import com.onsikku.onsikku_back.global.auth.dto.KakaoMemberInfo;
import com.onsikku.onsikku_back.global.auth.dto.KakaoSignupRequest;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.FamilyRepository;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.domain.member.util.InvitationCodeGenerator;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.jwt.JwtProvider;
import com.onsikku.onsikku_back.global.redis.RedisService;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.onsikku.onsikku_back.global.jwt.TokenConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private final InvitationCodeGenerator invitationCodeGenerator;
  private final KakaoOAuth2Service kakaoOAuth2Service;
  private final RegistrationTokenService registrationTokenService;
  private final MemberRepository memberRepository;
  private final FamilyRepository familyRepository;
  private final JwtProvider jwtProvider;
  private final RedisService redisService;
  private final QuestionService questionService;

  @Transactional
  public AuthResponse kakaoLoginWithCode(String code) {
    // 카카오 OAuth2 인증 코드로부터 사용자 정보를 가져옵니다.
    KakaoMemberInfo memberInfo = kakaoOAuth2Service.getKakaoMemberInfoFromCode(code);

    // 사용자 정보를 기반으로 회원 정보를 조회합니다.
    Optional<Member> existingMember = memberRepository.findByKakaoId(memberInfo.kakaoId());

    // 이미 등록된 회원인 경우 JWT 토큰을 생성하여 반환합니다.
    if (existingMember.isPresent()) {
      String refreshToken = jwtProvider.generateRefreshTokenFromMember(existingMember.get());
      redisService.set(RT_KEY_PREFIX + existingMember.get().getId().toString(), refreshToken, Duration.ofMillis(jwtProvider.getJwtRefreshExpirationInMs()));
      return AuthResponse.builder()
          .accessToken(jwtProvider.generateAccessTokenFromMember(existingMember.get()))
          .refreshToken(refreshToken)
          .isRegistered(true)
          .build();
    }
    // 등록되지 않은 회원인 경우, 등록 토큰을 생성하여 반환합니다.
    return AuthResponse.builder()
        .registrationToken(registrationTokenService.save(memberInfo))
        .isRegistered(false)
        .build();
  }

  @Transactional
  public AuthResponse register(KakaoSignupRequest request) {
    // 요청에서 등록 토큰을 사용하여 카카오 회원 정보를 가져옵니다.
    KakaoMemberInfo memberInfo = registrationTokenService.get(request.registrationToken());

    // 카카오 ID로 이미 등록된 회원이 있는지 확인합니다.
    if (memberRepository.existsByKakaoId(memberInfo.kakaoId())) {
      throw new BaseException(BaseResponseStatus.ALREADY_REGISTERED);
    }

    // 가족 모드에 따라 가족을 생성하거나 조회합니다.
    Family family = getOrCreateFamily(request);

    // 새로운 회원 정보를 생성합니다.
    Member member = Member.from(memberInfo, request, family);

    // 회원 정보를 저장하고, 등록 토큰을 삭제합니다.
    memberRepository.save(member);
    registrationTokenService.delete(request.registrationToken());

    questionService.generateAndAssignQuestionForFamily(family);

    // 리프레시 토큰을 생성하고 Redis에 저장합니다.
    String refreshToken = jwtProvider.generateRefreshTokenFromMember(member);
    redisService.set(RT_KEY_PREFIX + member.getId().toString(), refreshToken, Duration.ofMillis(jwtProvider.getJwtRefreshExpirationInMs()));

    return AuthResponse.builder()
        .isRegistered(true)
        .accessToken(jwtProvider.generateAccessTokenFromMember(member))
        .refreshToken(refreshToken)
        .build();
  }

  @Transactional
  public AuthResponse reissueToken(String refreshToken) {
    // 수신된 리프레시 토큰의 유효성을 검증합니다.
    Claims claims = jwtProvider.validateToken(refreshToken);
    jwtProvider.validateTokenType(claims, REFRESH_TOKEN_TYPE);

    //클레임에서 회원 ID를 추출합니다.
    UUID memberId = jwtProvider.getMemberIdFromClaims(claims);
    String redisKey = RT_KEY_PREFIX + memberId.toString();
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

    // 저장소(Redis)의 RT와 수신된 RT가 일치하는지 2차 검증합니다.
    String storedRT = redisService.get(redisKey, String.class);
    if (storedRT == null) {                 //  로그아웃되었거나, 토큰이 만료되어 자동 삭제된 경우입니다.
      log.warn("Refresh Token not found in Redis for memberId: {}", memberId);
      throw new BaseException(BaseResponseStatus.INVALID_REFRESH_TOKEN);
    }
    if (!storedRT.equals(refreshToken)) {   // 탈취 시도 또는 비정상적 접근 가능
      log.warn("Mismatch Refresh Token for memberId: {}. (Potential theft attempt)", memberId);
      redisService.delete(redisKey);        // 보안을 위해 해당 RT 삭제 처리
      throw new BaseException(BaseResponseStatus.INVALID_REFRESH_TOKEN);
    }
    // 새 Access Token, Refresh Token 발급
    String newAccessToken = jwtProvider.generateAccessTokenFromMember(member);
    String newRefreshToken = jwtProvider.generateRefreshTokenFromMember(member);

    // Redis에 새로운 Refresh Token 저장 (기존 RT 덮어쓰기)
    redisService.set(redisKey, newRefreshToken, Duration.ofMillis(jwtProvider.getJwtRefreshExpirationInMs()));

    return AuthResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  // 로그아웃 처리: Redis에서 Refresh Token 삭제
  @Transactional
  public void logout(UUID memberId, HttpServletRequest request) {
    String accessToken = jwtProvider.extractToken(request);
    // 회원의 Refresh Token 삭제
    String redisKey = RT_KEY_PREFIX + memberId.toString();
    redisService.delete(redisKey);
    log.info("Refresh Token deleted from Redis for member: {}", memberId);

    // Access Token 블랙리스트 추가 (남은 기간 동안 유효하지 않도록 설정)
    try {
      Claims claims = jwtProvider.validateToken(accessToken);
      // 토큰 타입 검증
      jwtProvider.validateTokenType(claims, ACCESS_TOKEN_TYPE);

      long remainingExpiration = jwtProvider.getRemainingExpirationInMs(claims);

      if (remainingExpiration > 0) {
        // AT 자체를 키로, 아무 값이나 넣고 남은 만료 시간만큼 TTL 설정
        String atBlacklistKey = AT_BLACKLIST_PREFIX + accessToken;
        redisService.set(atBlacklistKey, "logout", Duration.ofMillis(remainingExpiration));
        log.info("Access Token blacklisted for {}ms (Key: {})", remainingExpiration, atBlacklistKey);
      }
    } catch (BaseException ex) {
      // 이미 만료되었거나 유효하지 않은 AT일 수 있으므로 RT 삭제만 했으면 성공으로 간주
      log.warn("Access Token validation failed during logout: {}. Only RT deletion was completed.", ex.getMessage());
    }
  }

  // ------------------------- private 메소드 -------------------------

  private Family getOrCreateFamily(KakaoSignupRequest request) {
    // 가족 생성 모드인 경우, 새로운 가족을 생성 후 반환합니다.
    if (request.familyMode().equals(FamilyMode.CREATE)) {
      return familyRepository.save(
          Family.builder()
              .familyName(request.familyName())
              .invitationCode(invitationCodeGenerator.generate())
              .isFamilyInviteEnabled(true)
              .build()
      );
    }
    // 가족 초대 모드인 경우, 가족 초대 코드로 가족 조회 후 반환합니다.
    if (request.familyInvitationCode() == null || request.familyInvitationCode().isBlank()) {
      throw new BaseException(BaseResponseStatus.INVALID_FAMILY_INVITATION_CODE);
    }
    return familyRepository.findByInvitationCode(request.familyInvitationCode())
        .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_FAMILY_INVITATION_CODE));
  }
 @Transactional
  public AuthResponse testRegister(AuthTestRequest request) {
   if (request.familyInvitationCode() == null || request.familyInvitationCode().isBlank()) {
     throw new BaseException(BaseResponseStatus.INVALID_FAMILY_INVITATION_CODE);
   }
    Family family = familyRepository.findByInvitationCode(request.familyInvitationCode())
        .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_FAMILY_INVITATION_CODE));
    // 새로운 회원 정보를 생성
    Member member = Member.builder()
        .profileImageUrl("https://example.com/profile.jpg")
        .familyRole(request.familyRole())
        .family(family)
        .birthDate(request.birthDate())
        .nickname(UUID.randomUUID().toString())
        .kakaoId(UUID.randomUUID().toString())
        .isAlarmEnabled(true)
        .build();
    // 회원 정보 저장
    memberRepository.save(member);
    // 리프레시 토큰을 생성하고 Redis에 저장합니다.
    String refreshToken = jwtProvider.generateRefreshTokenFromMember(member);
    redisService.set(RT_KEY_PREFIX + member.getId().toString(), refreshToken, Duration.ofMillis(jwtProvider.getJwtRefreshExpirationInMs()));
    return AuthResponse.builder()
        .isRegistered(true)
        .accessToken(jwtProvider.generateAccessTokenFromMember(member))
        .refreshToken(refreshToken)
        .build();
  }
}