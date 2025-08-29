package com.onsikku.onsikku_back.domain.answer.repository;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.question.domain.Question;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findAllByQuestion(QuestionAssignment question);
    //@Query("SELECT DISTINCT a.question FROM Answer a WHERE a.user.id = :userId")
}