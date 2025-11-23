package com.onsikku.onsikku_back.domain.answer.controller;

import com.onsikku.onsikku_back.domain.answer.dto.CommentRequest;
import com.onsikku.onsikku_back.domain.answer.dto.CommentResponse;
import com.onsikku.onsikku_back.domain.answer.service.CommentService;
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
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(
    name = "댓글 API",
    description = "댓글 CRUD API"
)
public class CommentController {
  private final CommentService commentService;

  @PostMapping
  @Operation(
      summary = "댓글 생성",
      description = """
    질문에 대한 댓글을 생성합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 답변이 달렸을 경우만 댓글을 작성할 수 있습니다.
    """
  )
  public BaseResponse<CommentResponse> createComment(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                     @Valid @RequestBody CommentRequest request) {
    return new BaseResponse<>(commentService.createComment(request, customUserDetails.getMember()));
  }

  @PatchMapping
  @Operation(
      summary = "댓글 수정",
      description = """
    댓글을 수정합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 댓글 작성자만 해당 댓글을 수정할 수 있습니다.
    """
  )
  public BaseResponse<CommentResponse> updateComment(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                     @Valid @RequestBody CommentRequest request) {
    return new BaseResponse<>(commentService.updateComment(request, customUserDetails.getMember()));
  }

  @DeleteMapping("{commentId}")
  @Operation(
      summary = "댓글 삭제",
      description = """
    댓글을 삭제합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 댓글 작성자만 해당 댓글을 삭제할 수 있습니다.
    """
  )
  public BaseResponse<Void> deleteComment(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                          @PathVariable UUID commentId) {
    commentService.deleteComment(commentId, customUserDetails.getMember());
    return new BaseResponse<>(BaseResponseStatus.SUCCESS);
  }
}
