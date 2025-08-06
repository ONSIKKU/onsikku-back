package com.onsikku.onsikku_back.domain.qna;


import com.onsikku.onsikku_back.domain.qna.domain.Question;
import com.onsikku.onsikku_back.domain.qna.repository.QuestionJpaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class QuestionInitializer {

    private final QuestionJpaRepository questionRepository;

    public QuestionInitializer(QuestionJpaRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @PostConstruct
    public void init() {
        if (questionRepository.count() == 0) {
            questionRepository.save(
                    Question.builder()
                            .content("Default Content")
                            .build()
            );
            System.out.println("Default question added at startup.");
        }
    }
}
