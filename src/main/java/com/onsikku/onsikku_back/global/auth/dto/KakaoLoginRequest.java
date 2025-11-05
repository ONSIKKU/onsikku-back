package com.onsikku.onsikku_back.global.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 로그인 요청 DTO")
public record KakaoLoginRequest(
    @Schema(description = "카카오 code", example = "asbsR-alecoyPgSME60p3EymUEMinCv0o9fnLWrzVxuwAAAAQKK2487Ho6jj-qNQmaAdvd990e411e7104")
    String code
)
{}
