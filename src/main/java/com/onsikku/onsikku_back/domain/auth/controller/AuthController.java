package com.onsikku.onsikku_back.domain.auth.controller;

import com.onsikku.onsikku_back.domain.auth.dto.KakaoLoginRequest;
import com.onsikku.onsikku_back.domain.auth.dto.KakaoLoginResponse;
import com.onsikku.onsikku_back.domain.auth.service.AuthService;
import com.onsikku.onsikku_back.domain.auth.dto.KakaoSignupRequest;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/kakao")
    public BaseResponse<KakaoLoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        return new BaseResponse<>(authService.kakaoLoginWithCode(request.code()));
    }


    @PostMapping("/signup")
    public BaseResponse<KakaoLoginResponse> signup(@RequestBody KakaoSignupRequest request) {
        return new BaseResponse<>(authService.register(request));
    }
}