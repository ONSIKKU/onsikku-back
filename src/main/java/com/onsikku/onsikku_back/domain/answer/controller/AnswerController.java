package com.onsikku.onsikku_back.domain.answer.controller;

import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.answer.service.AnswerService;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    // 답변 생성
    @PostMapping("/answers")
    public BaseResponse<Answer> createAnswer(@RequestBody AnswerRequest request,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return new BaseResponse<>(answerService.createAnswer(request, customUserDetails.getMember()));
    }

    // 특정 질문 답변 조회
    @PostMapping("/answers/{questionAssignmentId}")
    public BaseResponse<List<Answer>> getAnswers(@PathVariable UUID questionAssignmentId,
                                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(answerService.findAnswer(questionAssignmentId, customUserDetails.getMember()));
    }

    // 답변 수정
    @PutMapping("/answers")
    public BaseResponse<Answer> updateAnswer(@Valid @RequestBody AnswerRequest request,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(answerService.updateAnswer(request, customUserDetails.getMember()));
    }

    // 답변 삭제 - 테스트용
    @DeleteMapping("/answers")
    public BaseResponse<String> deleteAnswer(@RequestBody AnswerRequest request) {
        answerService.deleteAnswer(request.id());
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }
}