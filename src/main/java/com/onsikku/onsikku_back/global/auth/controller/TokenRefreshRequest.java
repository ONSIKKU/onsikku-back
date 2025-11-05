package com.onsikku.onsikku_back.global.auth.controller;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenRefreshRequest {
    private String refreshToken;
}
