package com.onsikku.onsikku_back.domain.qna.repository;

import com.onsikku.onsikku_back.domain.qna.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByIdIn(List<Long> ids);
}