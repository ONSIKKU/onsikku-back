package com.onsikku.onsikku_back.global.auth.dto;

import com.onsikku.onsikku_back.domain.member.domain.FamilyRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "카카오 회원가입 요청 DTO")
public record AuthTestRequest(
    @Schema(description = "가족 내 역할 ENUM (MOTHER, FATHER, DAUGHTER, SON, GRANDMOTHER, GRANDFATHER)", example = "FATHER")
    FamilyRole familyRole,

    @Schema(description = "생년월일", example = "2000-01-01")
    LocalDate birthDate,

    @Schema(description = "가족 초대 코드 (대문자 영문 및 숫자 8자, 가족 모드 JOIN 시 필수)", example = "I12D34O2")
    String familyInvitationCode
) {}