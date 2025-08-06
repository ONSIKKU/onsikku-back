package com.onsikku.onsikku_back.domain.qna.service;


import com.onsikku.onsikku_back.domain.qna.domain.Question;
import com.onsikku.onsikku_back.domain.qna.repository.QuestionJpaRepository;
import com.onsikku.onsikku_back.global.exception.BaseException;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import org.springframework.stereotype.Component;

@Component
public class QnaServiceHelper {
    private final QuestionJpaRepository questionRepository;

    public QnaServiceHelper(QuestionJpaRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Question findQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_QUESTION));
    }

    public Question findNextQuestion(Long questionId) {
        return questionRepository.findById(questionId + 1)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_QUESTION));
    }

//    public List<Question> findAllQuestions(User user) {
//        return answerRepository.findDistinctQuestionByUser(user);
//    }

}
