package com.onsikku.onsikku_back.domain.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionInstanceRepository extends JpaRepository<QuestionInstanceRepository, Long> {
}
