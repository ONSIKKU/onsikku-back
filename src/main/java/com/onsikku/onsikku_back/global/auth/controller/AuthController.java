package com.onsikku.onsikku_back.global.auth.controller;

import com.onsikku.onsikku_back.domain.member.domain.SocialType;
import com.onsikku.onsikku_back.global.auth.dto.*;
import com.onsikku.onsikku_back.global.auth.dto.request.AppleLoginRequest;
import com.onsikku.onsikku_back.global.auth.dto.request.SocialSignupRequest;
import com.onsikku.onsikku_back.global.auth.dto.request.TokenRefreshRequest;
import com.onsikku.onsikku_back.global.auth.service.AuthService;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
    name = "인증 API",
    description = "인증 관련 API"
)
public class AuthController {
    private final AuthService authService;

    @GetMapping("/kakao/redirect")
    @Operation(
        summary = "카카오 로그인 (티켓 발급)",
        description = """
    **리다이렉트용 입니다.**
    code를 통해 카카오 로그인을 수행합니다.
    이후 임시 티켓을 발급하여, 리다이렉트합니다.
    ## 인증(JWT): **불필요**
    """
    )
    public void kakaoLogin(@RequestParam String code, HttpServletResponse response) throws IOException {
        // 코드를 이용한 로그인 로직 처리
        String ticket = authService.socialLogin(code, SocialType.KAKAO).getTicket();
        // 앱의 딥링크 주소로 리다이렉트
        response.sendRedirect("onsikku://auth?ticket=" + ticket);
    }
    // ---------------- [APPLE] POST 방식 ----------------
    @PostMapping("/apple")
    @Operation(
        summary = "애플 로그인 (iOS Native)",
        description = """
        **iOS 앱 내장 로그인 방식입니다.**
        Client가 Apple SDK로 받은 `identityToken`을 전송합니다.
        서버는 토큰을 검증하고, **티켓(ticket)**이 담긴 AuthResponse를 반환합니다.
        """
    )
    public BaseResponse<AuthResponse> appleLogin(@RequestBody AppleLoginRequest request) {
        // 애플은 code 대신 identityToken을 넘겨줍니다.
        AuthResponse authResponse = authService.socialLogin(request.identityToken(), SocialType.APPLE);

        return new BaseResponse<>(authResponse);
    }

    // ---------------- [공통] 티켓 교환 ----------------
    @GetMapping("/exchange")
    @Operation(summary = "티켓 교환 (공통)", description = """
    카카오/애플 로그인 후 발급받은 티켓을 확인하고, 저장된 유저의 AuthResponse를 반환합니다.
    ## 인증(JWT): **불필요**
    ## 참고사항
    - 회원가입이 되어 있지 않은 경우, registrationToken을 반환합니다.
    - 회원가입이 되어 있는 경우, JWT 토큰을 포함한 `KakaoLoginResponse`를 반환합니다.
    """
    )
    public BaseResponse<AuthResponse> exchangeTicket(@RequestParam String ticket) {
        return new BaseResponse<>(authService.exchangeTicket(ticket));
    }

    @PostMapping("/signup")
    @Operation(
        summary = "회원가입",
        description = """
    registrationToken을 통해 회원가입을 수행합니다.
    ## 인증(JWT): **불필요**
    ## 참고사항
    - `registrationToken`은 소셜 로그인 시 반환된 회원가입 토큰입니다.
    - 회원가입 후, JWT 토큰을 포함한 `AuthResponse`를 반환합니다.
    """
    )
    public BaseResponse<AuthResponse> signup(@RequestBody SocialSignupRequest request) {
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

    /*
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
     */
}