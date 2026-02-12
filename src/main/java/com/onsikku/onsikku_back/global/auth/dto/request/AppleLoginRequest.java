package com.onsikku.onsikku_back.global.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AppleLoginRequest(
    @Schema(description = "Apple 에서 받은 identityToken", example = "eyJraWQiOiJ...")
    String identityToken
) {}