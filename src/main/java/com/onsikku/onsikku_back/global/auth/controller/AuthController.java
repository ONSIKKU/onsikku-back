package com.onsikku.onsikku_back.global.auth.controller;

import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.global.auth.dto.KakaoLoginRequest;
import com.onsikku.onsikku_back.global.auth.dto.KakaoLoginResponse;
import com.onsikku.onsikku_back.global.auth.service.AuthService;
import com.onsikku.onsikku_back.global.auth.dto.KakaoSignupRequest;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public BaseResponse<KakaoLoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
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
    public BaseResponse<KakaoLoginResponse> signup(@RequestBody KakaoSignupRequest request) {
        return new BaseResponse<>(authService.register(request));
    }

    @DeleteMapping("/withdraw")
    @Operation(
        summary = "테스트용 회원 탈퇴",
        description = """
    현재 로그인한 사용자의 회원 탈퇴를 수행합니다.
    ## 인증(JWT): **필요**
    
    ## 참고사항
    - 이 API는 테스트용으로만 사용되며, 실제 서비스에서는 별도의 회원 탈퇴 절차가 필요합니다.
    - 회원 탈퇴 후, 성공 메시지를 반환합니다
    """
    )
    public BaseResponse<String> withdraw(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        authService.withdraw(customUserDetails.getMember().getId());
        return new BaseResponse<>("회원 탈퇴가 완료되었습니다.");
    }
}