package com.onsikku.onsikku_back.domain.answer.controller;

import com.onsikku.onsikku_back.domain.answer.domain.Answer;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(
    name = "답변 API",
    description = "질문에 대한 답변 생성, 조회, 수정 API"
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
    """
    )
    public BaseResponse<Answer> createAnswer(@RequestBody AnswerRequest request,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(answerService.createAnswer(request, customUserDetails.getMember()));
    }

    @GetMapping("/answers/{questionAssignmentId}")
    @Operation(
        summary = "특정 질문 답변 조회",
        description = """
    특정 질문에 대한 답변을 조회합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 질문 ID를 경로 변수로 전달해야 합니다.
    """
    )
    public BaseResponse<List<Answer>> getAnswers(@PathVariable UUID questionAssignmentId,
                                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(answerService.findAnswer(questionAssignmentId, customUserDetails.getMember()));
    }

    @PutMapping("/answers")
    @Operation(
        summary = "답변 수정",
        description = """
    기존 답변을 수정합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 요청 본문에 질문 ID, 답변 ID, 수정된 내용을 포함해야 합니다.
    """
    )
    public BaseResponse<Answer> updateAnswer(@Valid @RequestBody AnswerRequest request,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(answerService.updateAnswer(request, customUserDetails.getMember()));
    }

    @DeleteMapping("/answers")
    @Operation(
        summary = "테스트용 답변 삭제",
        description = """
    기존 답변을 삭제합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 테스트용입니다.
    """
    )

    public BaseResponse<String> deleteAnswer(@RequestBody AnswerRequest request) {
        answerService.deleteAnswer(request.id());
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }
}