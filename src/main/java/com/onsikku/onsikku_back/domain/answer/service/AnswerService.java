package com.onsikku.onsikku_back.domain.answer.service;


import com.onsikku.onsikku_back.domain.ai.domain.AnswerAnalysis;
import com.onsikku.onsikku_back.domain.ai.dto.request.AnswerAnalysisRequest;
import com.onsikku.onsikku_back.domain.ai.repository.AnswerAnalysisRepository;
import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.repository.QuestionAssignmentRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final AiRequestService aiRequestService;
    private final AnswerAnalysisRepository answerAnalysisRepository;

    @Transactional
    public AnswerResponse createAnswer(AnswerRequest request, Member member) {
        QuestionAssignment assignment = findQuestionAssignmentAndAuthorizeFamily(request.questionAssignmentId(), member);
        if (!assignment.getMember().getId().equals(member.getId())) {
            throw new BaseException(BaseResponseStatus.CANNOT_ANSWER_OTHER_QUESTION);
        }
        // 중복 답변 확인
        List<Answer> answers = answerRepository.findByQuestionAssignmentId(request.questionAssignmentId());
        for(Answer answer : answers) {
            if(answer.getMember().getId().equals(member.getId())) {
                throw new BaseException(BaseResponseStatus.ALREADY_ANSWERED_QUESTION);
            }
        }
        Answer newAnswer = answerRepository.save(Answer.create(assignment, member, request.answerType(), request.content()));
        assignment.markAsAnswered();
        // AI 분석 요청
        aiRequestService.analyzeAnswer(newAnswer, AnswerAnalysisRequest.createFromAnswerAndQuestionInstance(newAnswer, assignment.getQuestionInstance()));
        return AnswerResponse.from(newAnswer);
    }

    @Transactional  // @Transactional에 의해 변경 감지(dirty checking)가 동작하므로 save를 호출할 필요가 없음
    public AnswerResponse updateAnswer(AnswerRequest request, Member member) {
        Answer answer = answerRepository.findById(request.answerId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
        if (!answer.getMember().getId().equals(member.getId())) {
            throw new BaseException(BaseResponseStatus.CANNOT_ANSWER_OTHER_QUESTION);
        }
        answer.updateContent(request.content());
        return AnswerResponse.from(answerRepository.save(answer));
    }

    // ---------------------------- 테스트용 ----------------------------
    @Transactional
    public void deleteAnswer(AnswerRequest request, Member member) {
        Answer answer = answerRepository.findById(request.answerId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
        QuestionAssignment questionAssignment = questionAssignmentRepository.findById(request.questionAssignmentId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_ASSIGNMENT_NOT_FOUND));
        questionAssignment.markAsSent(LocalDateTime.now().plusWeeks(1L));
        answerAnalysisRepository.deleteByAnswer(answer);
        answerRepository.delete(answer);
    }
    @Transactional
    public List<AnswerAnalysis> getAllAnswerAnalysis(Member member) {
        return answerAnalysisRepository.findAllAnalysesByMemberId(member.getId());
    }

    private QuestionAssignment findQuestionAssignmentAndAuthorizeFamily(UUID questionAssignmentId, Member member) {
        QuestionAssignment assignment = questionAssignmentRepository.findById(questionAssignmentId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_ASSIGNMENT_NOT_FOUND));
        if(!assignment.getMember().getFamily().getId().equals(member.getFamily().getId())) {
            throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
        }
        return assignment;
    }
}
