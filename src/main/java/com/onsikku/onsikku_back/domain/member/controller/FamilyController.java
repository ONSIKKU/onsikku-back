package com.onsikku.onsikku_back.domain.member.controller;


import com.onsikku.onsikku_back.domain.member.service.FamilyService;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/families")
public class FamilyController {
  private final FamilyService familyService;

  @GetMapping("{familyId}/home")
  public ResponseEntity<String> getFamilyHome(@PathVariable UUID familyId, @RequestParam LocalDate date,
                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    familyService.getFamilyHome(customUserDetails.getMember(), familyId, date);
    return ResponseEntity.ok("가족 홈 페이지");
  }
}
