package com.onsikku.onsikku_back.domain.ai.repository;

import com.onsikku.onsikku_back.domain.ai.entity.AnswerAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnswerAnalysisRepository extends JpaRepository<AnswerAnalysis, UUID> {

}
