package com.onsikku.onsikku_back.domain.question.controller;


import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.dto.QuestionRequest;
import com.onsikku.onsikku_back.domain.question.service.QuestionService;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
@Tag(
    name = "질문 API",
    description = "질문 관련 API"
)
public class QuestionController {
    private final QuestionService questionService;

    // 가족 별 질문 조회
    @GetMapping
    @Operation(
        summary = "오늘의 질문 조회",
        description = """
    특정 가족의 오늘의 질문을 조회합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 가장 오래된 미답변 질문을 먼저 조회합니다.
    - 미답변 질문 ID가 없다면, 가장 최신 질문을 조회합니다.
    - 반환은 질문 할당(QuestionAssignment) 리스트로 이루어집니다.
    - 없다면 빈 목록을 반환합니다.
    """
    )
    public BaseResponse<List<QuestionAssignment>> getTodayQuestion(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(questionService.findQuestions(customUserDetails.getMember()));
    }

    // 가족 별 지난 질문 조회 + 주인공들
    // TODO : Answer 포함해서 반환 고려
    @GetMapping("/monthly")
    @Operation(
        summary = "월별 질문 조회",
        description = """
    특정 가족의 특정 년도, 월의 질문들을 조회합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - year, month 쿼리 파라미터로 해당 월의 질문들을 조회합니다.
    - 반환은 질문 할당(QuestionAssignment) 리스트로 이루어집니다.
    - 없다면 빈 목록을 반환합니다.
    """
    )
    public BaseResponse<List<QuestionAssignment>> getQuestionsByMonthAndYear(
        @RequestParam("year") int year,
        @RequestParam("month") int month,
        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(questionService.findMonthlyQuestions(
            customUserDetails.getMember().getFamily(), year, month));
    }

    @DeleteMapping
    @Operation(
        summary = "테스트용 질문 삭제",
        description = """
    특정 질문을 삭제합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 테스트용으로만 사용됩니다.
    """
    )
    public BaseResponse<String> DeleteQuestion(@RequestBody QuestionRequest request) {
        questionService.deleteQuestion(request);
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }
}
