package com.onsikku.onsikku_back.domain.answer.service;


import com.onsikku.onsikku_back.domain.ai.domain.AnswerAnalysis;
import com.onsikku.onsikku_back.domain.ai.dto.request.AnswerAnalysisRequest;
import com.onsikku.onsikku_back.domain.ai.repository.AnswerAnalysisRepository;
import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.answer.dto.ReactionType;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.MemberQuestion;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.domain.question.repository.QuestionAssignmentRepository;
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
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final MemberQuestionRepository memberQuestionRepository;
    private final AiRequestService aiRequestService;
    private final AnswerAnalysisRepository answerAnalysisRepository;

    @Transactional
    public AnswerResponse createAnswer(AnswerRequest request, Member member) {
        MemberQuestion assignment =questionAssignmentRepository.findById(request.questionAssignmentId())
            .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_ASSIGNMENT_NOT_FOUND));
        authorizeFamily(assignment.getFamily().getId(), member);

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
        MemberQuestion instance = memberQuestionRepository.findById(assignment.getQuestionInstance().getId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_NOT_FOUND));
        // AI 분석 요청
        aiRequestService.analyzeAnswer(newAnswer, AnswerAnalysisRequest.createFromAnswerAndQuestionInstance(newAnswer, instance));
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

    @Transactional
    public AnswerResponse reactionAnswer(AnswerRequest request, Member member) {
        Answer answer = answerRepository.findById(request.answerId())
            .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
       authorizeFamily(answer.getFamily().getId(), member);
        if(request.reactionType().equals(ReactionType.LIKE)) {
            log.info("LIKE reaction received");
            answer.incrementLikeReaction();
        }
        else if(request.reactionType().equals(ReactionType.ANGRY)) {
            log.info("ANGRY reaction received");
            answer.incrementAngryReaction();
        }
        else if(request.reactionType().equals(ReactionType.SAD)) {
            log.info("SAD reaction received");
            answer.incrementSadReaction();
        }
        else if(request.reactionType().equals(ReactionType.FUNNY)) {
            log.info("FUNNY reaction received");
            answer.incrementFunnyReaction();
        }
        return AnswerResponse.from(answerRepository.save(answer));
    }

    // ---------------------------- 테스트용 ----------------------------
    @Transactional
    public void deleteAnswer(AnswerRequest request, Member member) {
        Answer answer = answerRepository.findById(request.answerId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
        MemberQuestion memberQuestion = questionAssignmentRepository.findById(request.questionAssignmentId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_ASSIGNMENT_NOT_FOUND));
        memberQuestion.markAsSent(LocalDateTime.now().plusWeeks(1L));
        answerAnalysisRepository.deleteByAnswer(answer);
        answerRepository.delete(answer);
    }
    @Transactional
    public List<AnswerAnalysis> getAllAnswerAnalysis(Member member) {
        return answerAnalysisRepository.findAllAnalysisByMemberId(member.getId());
    }

    private void authorizeFamily(UUID memberId, Member member) {
        if (!memberId.equals(member.getFamily().getId())) {
            throw new BaseException(BaseResponseStatus.CANNOT_ANSWER_OTHER_QUESTION);
        }
    }
}
