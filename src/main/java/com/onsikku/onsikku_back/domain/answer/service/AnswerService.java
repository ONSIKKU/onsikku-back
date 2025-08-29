package com.onsikku.onsikku_back.domain.answer.service;


import com.onsikku.onsikku_back.domain.answer.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.answer.dto.AnswerResponse;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.question.domain.Question;
import com.onsikku.onsikku_back.domain.answer.repository.AnswerRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;



    public Answer createAnswer(AnswerRequest request, Member member) {


        Answer.builder()
            .answerType()

        Answer answer =  answerRepository.save();
        return AnswerResponse.from(answer);
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



    // ---------------------------- 테스트용 ----------------------------
    public void deleteAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));

        answerRepository.delete(answer);
    }
}
