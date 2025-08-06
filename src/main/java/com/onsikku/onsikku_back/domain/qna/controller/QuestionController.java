package com.onsikku.onsikku_back.domain.qna.controller;


import com.onsikku.onsikku_back.domain.qna.dto.QuestionRequest;
import com.onsikku.onsikku_back.domain.qna.domain.Question;
import com.onsikku.onsikku_back.domain.qna.service.QuestionService;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
public class QuestionController {
    private final QuestionService questionService;
    private final MemberRepository memberRepository;

    public QuestionController(QuestionService questionService, MemberRepository memberRepository) {
        this.questionService = questionService;
        this.memberRepository = memberRepository;
    }

    // 질문 생성 (새롭게 추가된 엔드포인트)
    @PostMapping("/question")
    public BaseResponse<Question> createQuestion(@RequestBody QuestionRequest request) {
        return new BaseResponse<>(questionService
                .createQuestion(request));
    }

    // 질문 조회
    @GetMapping("/question")
    public BaseResponse<List<Question>> getQuestions(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Authenticated user: " + userDetails.getUsername());
        Member member = memberRepository.findByKakaoId(userDetails.getUsername())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        return new BaseResponse<>(questionService.findQuestions(member));
    }

    // 질문 수정
    @PutMapping("/question")
    public BaseResponse<Question> updateQuestion(@RequestBody QuestionRequest request) {
        return new BaseResponse<>(questionService.updateQuestion(request));
    }

    // 질문 삭제
    @DeleteMapping("/question")
    public BaseResponse<String> DeleteQuestion(@RequestBody QuestionRequest request) {
        questionService.deleteQuestion(request);
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }
}
