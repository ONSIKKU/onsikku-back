package com.onsikku.onsikku_back.domain.answer.controller;

import com.onsikku.onsikku_back.domain.ai.domain.AnswerAnalysis;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.answer.service.AnswerService;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(
    name = "답변 API",
    description = "질문에 대한 답변 생성, 수정 API"
)
public class AnswerController {
    private final AnswerService answerService;

    @PostMapping("/answers")
    @Operation(
        summary = "답변 생성",
        description = """
    질문에 대한 답변을 생성합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 요청 본문에 질문 ID와 답변 내용을 포함해야 합니다.
    - AnswerType 은 String ENUM 타입입니다 : TEXT, IMAGE, AUDIO, VIDEO, FILE, MIXED
    - JsonNode content 예시:
      {
        "text": "이것은 텍스트 답변입니다.",
        "images": [
          "https://example.com/image1.jpg",
          "https://example.com/image2.jpg"
        ]
      }
    """
    )
    public BaseResponse<AnswerResponse> createAnswer(@RequestBody AnswerRequest request,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(answerService.createAnswer(request, customUserDetails.getMember()));
    }

    @PatchMapping("/answers")
    @Operation(
        summary = "답변 수정",
        description = """
    기존 답변을 수정합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 요청 본문에 질문 ID, 답변 ID, 수정된 내용을 포함해야 합니다.
    - 내용 변경만 가능합니다.
    """
    )
    public BaseResponse<AnswerResponse> updateAnswer(@RequestBody AnswerRequest request,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(answerService.updateAnswer(request, customUserDetails.getMember()));
    }

    @PostMapping("/answers/reaction")
    @Operation(
        summary = "답변 반응 추가/수정",
        description = """
    답변에 대한 반응을 추가하거나 수정합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 요청 본문에 답변 ID, ReactionType을 포함해야 합니다.
    - ReactionType은 String ENUM 타입입니다 : LIKE, ANGRY, SAD, FUNNY
    """
    )
    public BaseResponse<AnswerResponse> reactionAnswer(@RequestBody AnswerRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(answerService.reactionAnswer(request, customUserDetails.getMember()));
    }


    @DeleteMapping("/test/answers")
    @Operation(
        summary = "테스트용 답변 삭제",
        description = """
    기존 답변을 삭제합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 테스트용입니다.
    """
    )
    public BaseResponse<String> deleteAnswer(@RequestBody AnswerRequest request,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        answerService.deleteAnswer(request, customUserDetails.getMember());
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }

    @GetMapping("/test/analysis")
    @Operation(
        summary = "테스트용 질문 분석 조회",
        description = """
    질문 분석 내용을 조회합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 테스트용입니다.
    - 본인이 작성한 질문에 대한 분석 결과만 반환됩니다.
    - 분석 결과는 QuestionAssignment 리스트로 반환됩니다.
    """
    )
    public BaseResponse<List<AnswerAnalysis>> getAnswerAnalysis(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(answerService.getAllAnswerAnalysis(customUserDetails.getMember()));
    }
}