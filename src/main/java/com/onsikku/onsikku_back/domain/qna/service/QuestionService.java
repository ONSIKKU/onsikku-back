package com.onsikku.onsikku_back.domain.qna.service;



import com.onsikku.onsikku_back.domain.qna.dto.QuestionRequest;
import com.onsikku.onsikku_back.domain.qna.domain.Question;
import com.onsikku.onsikku_back.domain.qna.repository.QuestionJpaRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.LongStream;

@Service
public class QuestionService {
    private final QuestionJpaRepository questionRepository;
    private final QnaServiceHelper qnaServiceHelper;

    public QuestionService(QuestionJpaRepository questionRepository, QnaServiceHelper qnaServiceHelper) {
        this.questionRepository = questionRepository;
        this.qnaServiceHelper = qnaServiceHelper;
    }

    public Question createQuestion(QuestionRequest request) {
        return questionRepository.save(Question.of(request));
    }

    public List<Question> findQuestions(Member member) {
        // Helper 메서드
        // User question id가 질문의 순서대로 이므로, 1부터 question_id 까지 리스트 제작후
        // 그 리스트만큼 질문 갖고오기
        Long questionId = 1L;
        List<Long> list = LongStream.rangeClosed(1, questionId)
                .boxed()
                .toList();
        return questionRepository.findAllByIdIn(list);
    }

    public Question updateQuestion(QuestionRequest request) {
        Question question = qnaServiceHelper.findQuestion(request.id());    // 내부 메서드
        question.updateQuestion(request);
        return questionRepository.save(question);
    }

    public void deleteQuestion(QuestionRequest request) {
        questionRepository.deleteById(request.id());
    }

}