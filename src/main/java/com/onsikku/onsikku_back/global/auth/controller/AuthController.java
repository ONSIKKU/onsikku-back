package com.onsikku.onsikku_back.global.auth.controller;

import com.onsikku.onsikku_back.global.auth.dto.AuthTestRequest;
import com.onsikku.onsikku_back.global.auth.dto.KakaoLoginRequest;
import com.onsikku.onsikku_back.global.auth.dto.AuthResponse;
import com.onsikku.onsikku_back.global.auth.service.AuthService;
import com.onsikku.onsikku_back.global.auth.dto.KakaoSignupRequest;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
    name = "인증 API",
    description = "인증 관련 API"
)
public class AuthController {
    private final AuthService authService;

    @PostMapping("/kakao")
    @Operation(
        summary = "카카오 로그인",
        description = """
    code를 통해 카카오 로그인을 수행합니다.
    ## 인증(JWT): **불필요**
    ## 참고사항
    - 회원가입이 되어 있지 않은 경우, registrationToken을 반환합니다.
    - 회원가입이 되어 있는 경우, JWT 토큰을 포함한 `KakaoLoginResponse`를 반환합니다.
    """
    )
    public BaseResponse<AuthResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        return new BaseResponse<>(authService.kakaoLoginWithCode(request.code()));
    }


    @PostMapping("/signup")
    @Operation(
        summary = "회원가입",
        description = """
    registrationToken을 통해 회원가입을 수행합니다.
    ## 인증(JWT): **불필요**
    ## 참고사항
    - `registrationToken`은 카카오 로그인 시 반환된 회원가입 토큰입니다.
    - 회원가입 후, JWT 토큰을 포함한 `KakaoLoginResponse`를 반환합니다.
    """
    )
    public BaseResponse<AuthResponse> signup(@RequestBody KakaoSignupRequest request) {
        return new BaseResponse<>(authService.register(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "JWT 토큰 재발급",
        description = """
    refreshToken을 통해 JWT 토큰을 재발급합니다.
    ## 인증(JWT): **불필요**
    
    ## 참고사항
    - refreshToken은 accessToken 대신 사용할 수 없습니다.
    - accessToken은 refreshToken 대신 사용할 수 없습니다.
    - 재발급 후에, refreshToken은 블랙리스트 처리되어 재사용이 불가능합니다.
    """
    )
    public BaseResponse<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return new BaseResponse<>(authService.reissueToken(request.getRefreshToken()));
    }

    @PostMapping("/test/signup")
    @Operation(
        summary = "테스트용 회원가입",
        description = """
    테스트용 목데이터 생성을 위한 회원가입을 수행합니다.
    ## 인증(JWT): **불필요**
    ## 참고사항 - 초대 전용입니다.
    ## 카카오 로그인 없이 동작하니, 회원 삭제 시 accesstoken 으로만 관리 가능합니다.
    ## 따라서 생성 후 access, refresh 토큰을 메모해주세요.
    """
    )
    public BaseResponse<AuthResponse> testSignup(@RequestBody AuthTestRequest request) {
        return new BaseResponse<>(authService.testRegister(request));
    }
}