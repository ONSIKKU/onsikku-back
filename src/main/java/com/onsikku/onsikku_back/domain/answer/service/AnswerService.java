package com.onsikku.onsikku_back.domain.answer.service;


import com.onsikku.onsikku_back.domain.ai.service.AiRequestService;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.repository.QuestionAssignmentRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionAssignmentRepository questionAssignmentRepository;
    private final AiRequestService aiRequestService;

    public List<Answer> findAnswer(UUID questionAssignmentId, Member member) {
        findQuestionAssignment(questionAssignmentId, member);
        return answerRepository.findByQuestionAssignmentId(questionAssignmentId);
    }

    // TODO : 답변 생성 후 답변 분석 요청 타이밍 고려
    @Transactional
    public Answer createAnswer(AnswerRequest request, Member member) {
        QuestionAssignment assignment = findQuestionAssignment(request.questionAssignmentId(), member);
        if (!assignment.getMember().getId().equals(member.getId())) {
            throw new BaseException(BaseResponseStatus.CANNOT_ANSWER_OTHER_QUESTION);
        }
        Answer newAnswer = Answer.create(assignment, member, request.answerType(), request.content());
        assignment.markAsAnswered();
        // AI 분석 요청
        // aiRequestService.analyzeAnswer(newAnswer, request 만들기..);
        return answerRepository.save(newAnswer);
    }

    @Transactional  // @Transactional에 의해 변경 감지(dirty checking)가 동작하므로 save를 호출할 필요가 없음
    public Answer updateAnswer(AnswerRequest request, Member member) {
        Answer answer = answerRepository.findById(request.id())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
        if (!answer.getMember().getId().equals(member.getId())) {
            throw new BaseException(BaseResponseStatus.CANNOT_ANSWER_OTHER_QUESTION);
        }
        answer.updateContent(request.content());
        return answer;
    }

    // ---------------------------- 테스트용 ----------------------------
    public void deleteAnswer(UUID answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));

        answerRepository.delete(answer);
    }

    private QuestionAssignment findQuestionAssignment(UUID questionAssignmentId, Member member) {
        QuestionAssignment assignment = questionAssignmentRepository.findById(questionAssignmentId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_ASSIGNMENT_NOT_FOUND));
        if(!assignment.getMember().getFamily().getId().equals(member.getFamily().getId())) {
            throw new BaseException(BaseResponseStatus.INVALID_FAMILY_MEMBER);
        }
        return assignment;
    }
}
