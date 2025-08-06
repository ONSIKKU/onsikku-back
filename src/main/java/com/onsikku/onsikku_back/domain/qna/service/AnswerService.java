package com.onsikku.onsikku_back.domain.qna.service;


import com.onsikku.onsikku_back.domain.qna.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.qna.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.qna.domain.Answer;
import com.onsikku.onsikku_back.domain.qna.domain.Question;
import com.onsikku.onsikku_back.domain.qna.repository.AnswerJpaRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnswerService {
    private final AnswerJpaRepository answerRepository;
    private final QnaServiceHelper qnaServiceHelper;

    public AnswerService(AnswerJpaRepository answerRepository, QnaServiceHelper qnaServiceHelper) {
        this.answerRepository = answerRepository;
        this.qnaServiceHelper = qnaServiceHelper;
    }

    public Answer createAnswer(AnswerRequest request, Member member) {
        // Helper 메서드
        Question question = qnaServiceHelper.findQuestion(request.questionId());
        // 다음 질문으로 업데이트 -> id + 1
        Question nextQuestion = qnaServiceHelper.findNextQuestion(request.questionId());
        return answerRepository.save(Answer.of(question, request, member));
    }

    public List<AnswerResponse> findAnswers(Long questionId) {
        // Helper 메서드
        Question question = qnaServiceHelper.findQuestion(questionId);
        List<Answer> answerList = answerRepository.findAllByQuestion(question);
        // dto 리스트로 옮기기
        List<AnswerResponse> responseList = new ArrayList<>();
        for(Answer answer : answerList) {
            responseList.add(AnswerResponse.from(answer));
        }
        return responseList;
    }

    public Answer updateAnswer(AnswerRequest request, Member member) {
        Answer answer = answerRepository.findById(request.id())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
        if (!answer.getMember().equals(member)) {
            throw new BaseException(BaseResponseStatus.INVALID_MEMBER);
        }

        answer.updateContent(request.content());
        return answerRepository.save(answer);
    }

    public void deleteAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));

        answerRepository.delete(answer);
    }
}
