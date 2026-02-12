package com.onsikku.onsikku_back.domain.member.dto;

import com.onsikku.onsikku_back.domain.member.domain.FamilyRole;

import java.time.LocalDate;
import org.openapitools.jackson.nullable.JsonNullable;
import io.swagger.v3.oas.annotations.media.Schema;

public record MypageRequest(
    @Schema(description = "가족에게 불릴 별명", example = "귀염둥이 막내")
    JsonNullable<String> nickname,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    JsonNullable<String> profileImageUrl,

    @Schema(description = "가족 내 역할 ENUM (MOTHER, FATHER, DAUGHTER, SON, GRANDMOTHER, GRANDFATHER)", example = "SON")
    JsonNullable<FamilyRole> familyRole,

    @Schema(description = "생년월일 (yyyy-MM-dd 형식)", example = "2000-05-15")
    JsonNullable<LocalDate> birthDate,

    @Schema(description = "가족 초대 가능 여부 (true면 새 코드 발급, false면 코드 삭제)", example = "true")
    JsonNullable<Boolean> isFamilyInviteEnabled
) {}


