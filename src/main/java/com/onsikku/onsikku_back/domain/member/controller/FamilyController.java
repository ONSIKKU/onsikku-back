package com.onsikku.onsikku_back.domain.member.controller;


import com.onsikku.onsikku_back.domain.member.service.FamilyService;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/families")
@Tag(
    name = "가족 API",
    description = "가족 홈 등 가족 관련 API"
)
public class FamilyController {
  private final FamilyService familyService;

  @GetMapping("{familyId}/home")
  @Operation(
      summary = "가족 홈 페이지 조회",
      description = """
    특정 날짜에 대한 가족 홈 페이지 정보를 조회합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 구현 예정
    """
  )
  public ResponseEntity<String> getFamilyHome(@PathVariable UUID familyId, @RequestParam LocalDate date,
                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    familyService.getFamilyHome(customUserDetails.getMember(), familyId, date);
    return ResponseEntity.ok("가족 홈 페이지");
  }
}
