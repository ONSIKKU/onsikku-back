package com.onsikku.onsikku_back.domain.answer.repository;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
  List<Answer> findByQuestionAssignmentId(UUID questionAssignmentId);
  Optional<Answer> findTopByMember_Family_IdOrderByCreatedAtDesc(UUID familyId);
  @Query("SELECT a FROM Answer a JOIN FETCH a.member WHERE a.questionInstance.id = :instanceId")
  List<Answer> findAllByQuestionInstanceId(UUID instanceId);
  @Query("SELECT a FROM Answer a JOIN FETCH a.member WHERE a.questionInstance.id IN :instanceIds")
  List<Answer> findAllByQuestionInstance_IdIn(List<UUID> instanceIds);

  void deleteByMember(Member member);

  int deleteAllByQuestionInstanceIn(List<QuestionInstance> questionInstances);
}