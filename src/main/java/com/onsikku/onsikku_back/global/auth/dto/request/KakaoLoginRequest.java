package com.onsikku.onsikku_back.global.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 로그인 요청 DTO")
public record KakaoLoginRequest(
    @Schema(description = "카카오 code")
    String code
)
{}
