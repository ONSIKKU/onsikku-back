package com.onsikku.onsikku_back.global.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenRefreshRequest {
    private String refreshToken;
}
