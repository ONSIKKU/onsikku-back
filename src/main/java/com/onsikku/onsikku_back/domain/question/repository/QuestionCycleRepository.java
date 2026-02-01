package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.question.domain.QuestionCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionCycleRepository extends JpaRepository<QuestionCycle, UUID> {

  Optional<QuestionCycle> findByFamily_Id(UUID familyId);
}
