package com.onsikku.onsikku_back.global.auth.dto;

import com.onsikku.onsikku_back.global.auth.domain.FamilyMode;
import com.onsikku.onsikku_back.domain.member.domain.FamilyRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "소셜 회원가입 요청 DTO")
public record SocialSignupRequest(

    @Schema(description = "회원가입 인증 토큰", example = "asbsdvd-9515-4661-a26e-990e411e7104")
    String registrationToken,

    @Schema(description = "가족 내 역할 ENUM (MOTHER, FATHER, DAUGHTER, SON, GRANDMOTHER, GRANDFATHER)", example = "SON")
    FamilyRole familyRole,

    @Schema(description = "가족에게 불릴 별명", example = "귀염둥이 막내")
    String nickname,

    @Schema(description = "생년월일", example = "2000-01-01")
    LocalDate birthDate,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String profileImageUrl,

    @Schema(description = "가족 모드 (CREATE 또는 JOIN)", example = "CREATE")
    FamilyMode familyMode,

    @Schema(description = "가족 이름(가족 모드 JOIN 시 필수)", example = "홍길동 가족")
    String familyName,

    @Schema(description = "가족 초대 코드 (대문자 영문 및 숫자 8자, 가족 모드 JOIN 시 필수)", example = "I12D34O2")
    String familyInvitationCode
) {}