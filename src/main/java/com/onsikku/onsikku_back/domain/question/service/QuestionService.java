package com.onsikku.onsikku_back.domain.question.service;



import com.onsikku.onsikku_back.domain.question.dto.QuestionRequest;
import com.onsikku.onsikku_back.domain.question.domain.Question;
import com.onsikku.onsikku_back.domain.question.repository.QuestionAssignmentRepository;
import com.onsikku.onsikku_back.domain.question.repository.QuestionJpaRepository;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private QuestionAssignmentRepository questionAssignmentRepository;

    public void findQuestions(Member member) {

    }

    public void deleteQuestion(QuestionRequest request) {
        questionAssignmentRepository.deleteById(request.id());
    }

}