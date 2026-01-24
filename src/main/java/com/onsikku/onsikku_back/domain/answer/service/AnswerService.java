package com.onsikku.onsikku_back.domain.answer.service;


import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.MemberQuestion;
import com.onsikku.onsikku_back.domain.question.repository.MemberQuestionRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final MemberQuestionRepository memberQuestionRepository;
    private final AiRequestService aiRequestService;

    @Transactional
    public AnswerResponse createAnswer(AnswerRequest request, Member member) {
        MemberQuestion memberQuestion = memberQuestionRepository.findById(request.questionAssignmentId())
            .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_QUESTION_NOT_FOUND));
        authorizeFamily(memberQuestion.getFamily().getId(), member);

        if (!memberQuestion.getMember().getId().equals(member.getId())) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED_FOR_RESOURCE);
        }
        // 중복 답변 확인
        List<Answer> answers = answerRepository.findByQuestionAssignmentId(request.questionAssignmentId());
        for(Answer answer : answers) {
            if(answer.getMember().getId().equals(member.getId())) {
                throw new BaseException(BaseResponseStatus.ALREADY_ANSWERED_QUESTION);
            }
        }
        Answer newAnswer = answerRepository.save(Answer.create(memberQuestion, member, request.answerType(), request.content()));
        memberQuestion.markAsAnswered();
        // AI 분석 요청
        //aiRequestService.analyzeAnswer(newAnswer);
        return AnswerResponse.from(newAnswer);
    }

    @Transactional
    public AnswerResponse updateAnswer(AnswerRequest request, Member member) {
        Answer answer = answerRepository.findById(request.answerId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
        if (!answer.getMember().getId().equals(member.getId())) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED_FOR_RESOURCE);
        }
        answer.updateContent(request.content());
        return AnswerResponse.from(answerRepository.save(answer));
    }

    @Transactional
    public AnswerResponse reactionAnswer(AnswerRequest request, Member member) {
        Answer answer = answerRepository.findById(request.answerId())
            .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
        authorizeFamily(answer.getFamily().getId(), member);

        return AnswerResponse.from(answerRepository.save(answer));
    }

    // ---------------------------- 테스트용 ----------------------------
    @Transactional
    public void deleteAnswer(AnswerRequest request, Member member) {
        Answer answer = answerRepository.findById(request.answerId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
        MemberQuestion memberQuestion = memberQuestionRepository.findById(request.questionAssignmentId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MEMBER_QUESTION_NOT_FOUND));
        memberQuestion.markAsSent(LocalDateTime.now(), LocalDateTime.now().plusWeeks(1));
        answerRepository.delete(answer);
    }

    private void authorizeFamily(UUID memberId, Member member) {
        if (!memberId.equals(member.getFamily().getId())) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED_FOR_RESOURCE);
        }
    }
}
