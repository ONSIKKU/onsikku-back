package com.onsikku.onsikku_back.domain.member.dto;

import com.onsikku.onsikku_back.domain.member.domain.FamilyRole;
import com.onsikku.onsikku_back.domain.member.domain.Gender;

import java.time.LocalDate;
import org.openapitools.jackson.nullable.JsonNullable;
import io.swagger.v3.oas.annotations.media.Schema;

public record MypageRequest(

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    JsonNullable<String> profileImageUrl,

    @Schema(description = "가족 내 역할 ENUM (PARENT, CHILD, GRANDPARENT)", example = "PARENT")
    JsonNullable<FamilyRole> familyRole,

    @Schema(description = "생년월일 (yyyy-MM-dd 형식)", example = "2000-05-15")
    JsonNullable<LocalDate> birthDate,

    @Schema(description = "성별 ENUM (MALE, FEMALE)", example = "MALE")
    JsonNullable<Gender> gender,

    @Schema(description = "알림 수신 여부", example = "true")
    JsonNullable<Boolean> isAlarmEnabled,

    @Schema(description = "가족 초대코드 재발급 여부 (true면 새 코드 발급)", example = "true")
    JsonNullable<Boolean> regenerateFamilyInvitationCode
) {}


