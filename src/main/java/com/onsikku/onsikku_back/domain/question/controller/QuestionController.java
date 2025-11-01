package com.onsikku.onsikku_back.domain.question.controller;


import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.dto.QuestionRequest;
import com.onsikku.onsikku_back.domain.question.service.QuestionService;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;

    // 가족 별 질문 조회
    @GetMapping()
    public BaseResponse<List<QuestionAssignment>> getTodayQuestion(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(questionService.findQuestions(customUserDetails.getMember()));
    }

    // 가족 별 지난 질문 조회 + 주인공들
    // TODO : Answer 포함해서 반환 고려
    @GetMapping("/monthly")
    public BaseResponse<List<QuestionAssignment>> getQuestionsByMonthAndYear(
        @RequestParam("year") int year,
        @RequestParam("month") int month,
        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(questionService.findMonthlyQuestions(
            customUserDetails.getMember().getFamily(), year, month));
    }


    // 질문 삭제 - 테스트용
    @DeleteMapping()
    public BaseResponse<String> DeleteQuestion(@RequestBody QuestionRequest request) {
        questionService.deleteQuestion(request);
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }
}
