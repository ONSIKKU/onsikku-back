package com.onsikku.onsikku_back.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 회원가입 요청 DTO")
public record KakaoSignupRequest(

    @Schema(description = "회원가입 인증 토큰", example = "asbsdvd-9515-4661-a26e-990e411e7104")
    String registrationToken,

    @Schema(description = "관계", example = "둘째 아들")
    String relation,

    @Schema(description = "전화번호", example = "010-1234-5678")
    String phoneNumber,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String profileImageUrl,

    @Schema(description = "가족 이름", example = "홍길동 가족")
    String familyName,

    @Schema(description = "가족 초대 코드 (대문자 영문 및 숫자 8자, 가족 모드 JOIN 시 필수)", example = "I12D34O2")
    String familyInvitationCode,

    @Schema(description = "가족 모드 (CREATE 또는 JOIN)", example = "CREATE")
    String familyMode
) {}