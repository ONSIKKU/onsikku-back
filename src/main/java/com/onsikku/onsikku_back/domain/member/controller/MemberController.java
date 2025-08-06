package com.onsikku.onsikku_back.domain.member.controller;


import com.onsikku.onsikku_back.domain.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.domain.member.dto.MypageResponse;
import com.onsikku.onsikku_back.domain.member.service.MemberService;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

  private final MemberService memberService;

  @GetMapping("/login")
  public String login() {
    return "로그인 페이지";
  }

  @GetMapping("/mypage")
  public BaseResponse<MypageResponse> getMyPage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return new BaseResponse<>(memberService.getMemberById(customUserDetails.getMember().getId()));
  }
}
