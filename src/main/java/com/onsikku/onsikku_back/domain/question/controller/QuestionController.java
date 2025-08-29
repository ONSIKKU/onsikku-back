package com.onsikku.onsikku_back.domain.question.controller;


import com.onsikku.onsikku_back.domain.question.dto.QuestionRequest;
import com.onsikku.onsikku_back.domain.question.domain.Question;
import com.onsikku.onsikku_back.domain.question.service.QuestionService;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/question")
public class QuestionController {
    private final QuestionService questionService;

    // 가족 별 모든 질문 조회

    // 질문 조회
    @GetMapping()
    public BaseResponse<Void> getQuestions(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        questionService.findQuestions(customUserDetails.getMember());
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }

    // 질문 삭제
    @DeleteMapping()
    public BaseResponse<String> DeleteQuestion(@RequestBody QuestionRequest request) {
        questionService.deleteQuestion(request);
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }
}
