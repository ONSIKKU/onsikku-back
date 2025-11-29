package com.onsikku.onsikku_back.domain.member.controller;


import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.domain.member.dto.MypageRequest;
import com.onsikku.onsikku_back.domain.member.dto.MypageResponse;
import com.onsikku.onsikku_back.domain.member.service.MemberService;
import com.onsikku.onsikku_back.global.auth.service.AuthService;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(
    name = "회원 API",
    description = "마이페이지 등 회원 관련 API"
)
public class MemberController {

  private final MemberService memberService;
  private final AuthService authService;

  @GetMapping("/login")
  public String login() {
    return "로그인 페이지";
  }

  @GetMapping("/mypage")
  @Operation(
      summary = "마이페이지 조회",
      description = """
    회원 마이페이지 정보를 조회합니다.
    ## 인증(JWT): **필요**
    """
  )
  public BaseResponse<MypageResponse> getMyPage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return new BaseResponse<>(memberService.getMemberById(customUserDetails.getMember().getId()));
  }
  @PatchMapping("/mypage")
  @Operation(
      summary = "마이페이지 수정 (부분 업데이트 지원)",
      description = """
    회원 마이페이지 정보를 수정합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - PATCH 메서드를 사용하며, JSON에서 원하는 필드만 포함하면 해당 필드만 수정됩니다.
    - 예: { "profileImageUrl": "https://example.com/new.jpg" }
    """
  )
  public BaseResponse<MypageResponse> updateMyPage(@RequestBody MypageRequest request,
                                                   @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return new BaseResponse<>(memberService.updateMemberById(request, customUserDetails.getMember().getId()));
  }

  @PostMapping("/logout")
  @Operation(
      summary = "로그아웃",
      description = """
    로그아웃을 수행합니다.
    Access Token의 사용자 정보를 기반으로, Refresh Token을 삭제합니다.
    현재 Access Token을 블랙리스트에 추가하여 즉시 무효화합니다.
    ## 인증(JWT): **필요 (Access Token)**
    """
  )
  public BaseResponse<String> logout(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    HttpServletRequest request) {
    authService.logout(userDetails.getMember().getId(), request);
    return new BaseResponse<>("성공적으로 로그아웃되었습니다.");
  }

  @PostMapping(value = "/delete")
  @Operation(
      summary = "회원 탈퇴",
      description = """
    회원 탈퇴를 진행합니다.
    회원이 생성한 답변, 질문 할당, 댓글 및 회원 데이터를 삭제합니다.
    탈퇴 후 해당 회원의 Refresh Token을 삭제하고, Access Token을 블랙리스트에 추가하여 즉시 무효화합니다.
    ## 인증(JWT): **필요**
    """
  )
  public BaseResponse<Void> deleteMember(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      HttpServletRequest request) {
    memberService.deleteMember(customUserDetails.getMember());
    authService.logout(customUserDetails.getMember().getId(), request);
    return new BaseResponse<>(BaseResponseStatus.SUCCESS);
  }
}
