package com.onsikku.onsikku_back.domain.qna.controller;

import com.onsikku.onsikku_back.domain.auth.domain.CustomUserDetails;
import com.onsikku.onsikku_back.domain.qna.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.qna.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.qna.domain.Answer;
import com.onsikku.onsikku_back.domain.qna.service.AnswerService;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.member.repository.MemberRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;
    private final MemberRepository memberRepository;

    // 답변 생성
    // todo : 유저의 답변 중복 생성 방지
    @PostMapping("/answer")
    public BaseResponse<AnswerResponse> createAnswer(@RequestBody AnswerRequest request, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Answer answer = answerService.createAnswer(request, customUserDetails.getMember());

        return new BaseResponse<>(AnswerResponse.from(answer));
    }

    // 답변 조회
    @GetMapping("/answer/{question_id}")
    public BaseResponse<List<AnswerResponse>> getAnswers(@PathVariable Long question_id) {
        return new BaseResponse<>(answerService.findAnswers(question_id));
    }

    // 답변 수정
    @PutMapping("/answer")
    public BaseResponse<AnswerResponse> updateAnswer(@Valid @RequestBody AnswerRequest request,
                                                     BindingResult bindingResult,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if(bindingResult.hasErrors()) {
            findErrorMessage(bindingResult);
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
        }

        Member member = memberRepository.findByKakaoId(userDetails.getUsername())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_NOT_FOUND));

        Answer answer = answerService.updateAnswer(request, member);
        return new BaseResponse<>(AnswerResponse.from(answer));
    }

    // 답변 삭제 - 테스트용
    @DeleteMapping("/answer/{answer_id}")
    public BaseResponse<String> deleteAnswer(@PathVariable Long answer_id) {
        answerService.deleteAnswer(answer_id);
        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }

    private void findErrorMessage(BindingResult bindingResult) {
        List<String> messages = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error ->  messages.add(error.getDefaultMessage()));
        for(String msg : messages) System.out.println(msg);

    }
}