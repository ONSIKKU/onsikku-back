package com.onsikku.onsikku_back.domain.answer.controller;

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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    // 답변 생성
    @PostMapping("/answer")
    public BaseResponse<AnswerResponse> createAnswer(@RequestBody AnswerRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return new BaseResponse<>(answerService.createAnswer(request, customUserDetails.getMember()));
    }

    // 답변 조회
    @GetMapping("/answer/{question_id}")
    public BaseResponse<List<AnswerResponse>> getAnswers(@PathVariable Long question_id) {
        return new BaseResponse<>(answerService.findAnswers(question_id));
    }

    // 답변 수정
    @PutMapping("/answer")
    public BaseResponse<AnswerResponse> updateAnswer(@Valid @RequestBody AnswerRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(AnswerResponse.from(answerService.updateAnswer(request, customUserDetails.getMember())
        ));
    }

    // 답변 삭제 - 테스트용
    @DeleteMapping("/answer/{answer_id}")
    public BaseResponse<String> deleteAnswer(@PathVariable Long answer_id) {
        answerService.deleteAnswer(answer_id);
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }
}