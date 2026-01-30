package com.onsikku.onsikku_back.domain.answer.controller;

import com.onsikku.onsikku_back.domain.answer.dto.ReactionRequest;
import com.onsikku.onsikku_back.domain.answer.service.ReactionService;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
@Tag(
    name = "반응 API",
    description = "답변에 대한 반응 API"
)
public class ReactionController {
  private final ReactionService reactionService;

  @PostMapping
  @Operation(
      summary = "리액션 생성",
      description = """
    답변에 대해 리액션을 남깁니다. (ReactionType : LIKE, ANGRY, SAD, FUNNY)
    ## 인증(JWT): **필요**
    ## 참고사항
    - 본인 가족의 답변에만 리액션을 남길 수 있습니다.
    - 한 답변에 대해 중복된 리액션을 남길 수 없습니다.
    """
  )
  public BaseResponse<Void> createReaction(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                           @Valid @RequestBody ReactionRequest request) {
    reactionService.createReaction(request.answerId(), request.type(), customUserDetails.getMember());
    return new BaseResponse<>(BaseResponseStatus.SUCCESS);
  }

  @DeleteMapping("/{answerId}")
  @Operation(
      summary = "리액션 삭제",
      description = """
    남긴 리액션을 삭제합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 본인이 작성한 리액션만 삭제할 수 있습니다.
    - answerId를 받고, 해당 답변에 본인이 남긴 리액션이 삭제됩니다.
    """
  )
  public BaseResponse<Void> deleteReaction(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                           @PathVariable UUID answerId) {
    reactionService.deleteReaction(answerId, customUserDetails.getMember());
    return new BaseResponse<>(BaseResponseStatus.SUCCESS);
  }
}