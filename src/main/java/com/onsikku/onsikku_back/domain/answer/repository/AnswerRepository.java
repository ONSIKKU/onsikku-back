package com.onsikku.onsikku_back.domain.answer.repository;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
  List<Answer> findByQuestionAssignmentId(UUID questionAssignmentId);
  Optional<Answer> findTopByMember_Family_IdOrderByCreatedAtDesc(UUID familyId);
  List<Answer> findAllByQuestionInstanceId(UUID questionInstanceId);

  List<Answer> findAllByQuestionInstanceIds(List<UUID> instanceIds);
}