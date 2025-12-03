package com.onsikku.onsikku_back.domain.ai.repository;

import com.onsikku.onsikku_back.domain.ai.domain.AnswerAnalysis;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnswerAnalysisRepository extends JpaRepository<AnswerAnalysis, UUID> {

  @Query("SELECT aa FROM AnswerAnalysis aa JOIN FETCH aa.answer a WHERE a.member.id = :memberId")
  List<AnswerAnalysis> findAllAnalysisByMemberId(@Param("memberId") UUID memberId);

  int deleteAllByAnswerIn(List<Answer> answers);

  void deleteByAnswer(Answer answer);

  AnswerAnalysis findByAnswer(Answer answer);
}
