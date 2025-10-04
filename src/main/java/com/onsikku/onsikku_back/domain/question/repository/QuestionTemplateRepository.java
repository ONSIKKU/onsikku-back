package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.question.domain.QuestionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionTemplateRepository extends JpaRepository<QuestionTemplate, UUID> {
}
