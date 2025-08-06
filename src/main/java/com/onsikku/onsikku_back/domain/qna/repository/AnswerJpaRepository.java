package com.onsikku.onsikku_back.domain.qna.repository;


import com.onsikku.onsikku_back.domain.qna.domain.Answer;
import com.onsikku.onsikku_back.domain.qna.domain.Question;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AnswerJpaRepository extends JpaRepository<Answer, Long> {
    List<Answer> findAllByQuestion(Question question);
    //@Query("SELECT DISTINCT a.question FROM Answer a WHERE a.user.id = :userId")
}