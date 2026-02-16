package com.onsikku.onsikku_back.domain.member.controller;


import com.onsikku.onsikku_back.domain.member.dto.BlockRequest;
import com.onsikku.onsikku_back.domain.member.dto.ReportRequest;
import com.onsikku.onsikku_back.domain.member.service.SafetyService;
import com.onsikku.onsikku_back.domain.question.dto.BlockedMemberResponse;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
@Tag(name = "safety API", description = "신고 및 차단 관련 API")
public class SafetyController {
    private final SafetyService safetyService;

    // 신고 API
    @PostMapping("/report")
    @Operation(
        summary = "콘텐츠 신고",
        description = """
            사용자가 부적절한 답변이나 댓글, 유저를 신고합니다.
            ## 인증(JWT): **필요**
            ## reason 값 (enum)
              SPAM("스팸 및 홍보성 콘텐츠"),
              INAPPROPRIATE_CONTENT("부적절한 콘텐츠 (음란물 등)"),
              ABUSIVE_LANGUAGE("욕설 및 비방, 혐오 표현"),
              PRIVACY_VIOLATION("개인정보 노출"),
              OTHER("기타");
            
            ## targetType 값 (enum)
              MEMBER,COMMENT, ANSWER
            
            ## 처리 프로세스
            - 신고 내용이 DB에 저장되며, 운영진이 24시간 이내에 검토합니다.
            
            ## 반환값 (String)
            성공시 : 신고가 정상적으로 접수되었습니다. 검토 후 처리하겠습니다.
            """
    )
    public ResponseEntity<String> reportContent(@AuthenticationPrincipal CustomUserDetails customUserDetails, // 인증된 사용자 ID
                                                @RequestBody ReportRequest request) {
        
        safetyService.createReport(customUserDetails.getMember().getId(), request);
        return ResponseEntity.ok("신고가 정상적으로 접수되었습니다. 검토 후 처리하겠습니다.");
    }

    // 차단 API
    @PostMapping("/block")
    @Operation(
        summary = "사용자 차단",
        description = """
            특정 사용자를 차단합니다. 차단된 사용자의 콘텐츠는 피드에서 필터링됩니다.
            ## 인증(JWT): **필요**
            ## 반환값 (String)
            성공시 : 해당 사용자를 차단했습니다. 앞으로 이 사용자의 글은 보이지 않습니다.
            """
    )
    public ResponseEntity<String> blockUser(@AuthenticationPrincipal CustomUserDetails customUserDetails,    // 인증된 사용자 ID
                                            @RequestBody BlockRequest request) {
        
        safetyService.blockUser(customUserDetails.getMember().getId(), request);
        return ResponseEntity.ok("해당 사용자를 차단했습니다. 앞으로 이 사용자의 글은 보이지 않습니다.");
    }

    // 차단 해제 API
    @DeleteMapping("/block")
    @Operation(
        summary = "사용자 차단 해제",
        description = """
            차단했던 사용자를 차단 해제합니다.
            ## 인증(JWT): **필요**
            ## 반환값 (String)
            성공시 : 차단이 해제되었습니다.
            """
    )
    public ResponseEntity<String> unblockUser(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                              @RequestBody BlockRequest request) {
        // blockUser -> unblockUser로 변경
        safetyService.unblockUser(customUserDetails.getMember().getId(), request);
        return ResponseEntity.ok("차단이 해제되었습니다.");
    }

    // 차단 목록 조회
    @GetMapping("/block")
    @Operation(
        summary = "차단한 사용자 목록 조회",
        description = """
            내가 차단한 사용자들의 목록을 조회합니다.
            ## 인증(JWT): **필요**
            ## 반환값 (List)
            - blockedId: 차단된 유저 ID
            - nickname: 차단된 유저 닉네임 (화면에 보여주기용)
            """
    )
    public ResponseEntity<List<BlockedMemberResponse>> getBlockedList(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(safetyService.getBlockedMemberList(customUserDetails.getMember().getId()));
    }
}