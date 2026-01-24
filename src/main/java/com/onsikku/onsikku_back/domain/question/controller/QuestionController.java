package com.onsikku.onsikku_back.domain.question.controller;


import com.onsikku.onsikku_back.domain.question.dto.QuestionResponse;
import com.onsikku.onsikku_back.domain.question.service.QuestionCycleService;
import com.onsikku.onsikku_back.domain.question.service.QuestionService;
import com.onsikku.onsikku_back.global.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
@Tag(
    name = "질문 API",
    description = "질문 관련 API"
)
public class QuestionController {
    private final QuestionService questionService;
    private final QuestionCycleService questionCycleService;

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
    - 가족 전체 정보와, QuestionInstance의 UUID와, QuestionAssignment 리스트로 반환됩니다.
    - assignmentState enums : PENDING, SENT, READ, ANSWERED, EXPIRED, FAILED
    """
    )
    public BaseResponse<QuestionResponse> getTodayQuestion(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(questionService.getTodayMemberQuestion(customUserDetails.getMember()));
    }

    @GetMapping("/{memberQuestionId}")
    @Operation(
        summary = "질문 인스턴스 상세 조회",
        description = """
    특정 질문 인스턴스의 상세 정보를 조회합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - memberQuestionId 경로 변수로 특정 질문 인스턴스를 조회합니다.
    - 반환은 questionDetails 객체로 이루어집니다.
    """
    )
    public BaseResponse<QuestionResponse> getQuestionInstanceDetails(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                     @PathVariable UUID memberQuestionId) {
        return new BaseResponse<>(questionService.findQuestionDetails(customUserDetails.getMember(), memberQuestionId));
    }

    // 가족 별 지난 질문 조회
    @GetMapping("/monthly")
    @Operation(
        summary = "월별 질문 조회",
        description = """
    특정 가족의 특정 년도, 월의 질문들을 조회합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - year, month 쿼리 파라미터로 해당 월의 질문들을 조회합니다.
    - 성능 이슈로 답변 등의 정보는 제외하고, questionDetails 리스트만 반환합니다.
    - 없다면 빈 목록을 반환합니다.
    """
    )
    public BaseResponse<QuestionResponse> getQuestionsByMonthAndYear(
        @RequestParam("year") int year,
        @RequestParam("month") int month,
        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new BaseResponse<>(questionService.findMonthlyQuestions(customUserDetails.getMember().getFamily(), year, month));
    }

    @GetMapping("/test/generate")
    @Operation(
        summary = "테스트용 질문 생성",
        description = """
    질문을 생성합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 테스트용으로만 사용됩니다.
    """
    )
    public BaseResponse<String> getAllQuestions(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        questionCycleService.getOrGenerateCycleAndAssignQuestionForFamily(customUserDetails.getMember().getFamily());
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }

    @GetMapping("test/delete")
    @Operation(
        summary = "테스트용 질문 삭제 (답변, 댓글 포함)",
        description = """
    특정 질문을 삭제합니다.
    ## 인증(JWT): **필요**
    ## 참고사항
    - 테스트용으로만 사용됩니다.
    - 특정 가족의 모든 질문 인스턴스, 할당, 답변, 댓글이 사라집니다.
    """
    )
    public BaseResponse<String> deleteFamilyData(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        questionService.deleteFamilyData(customUserDetails.getMember().getFamily().getId());
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }
}
