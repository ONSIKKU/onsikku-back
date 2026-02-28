package com.onsikku.onsikku_back.domain.answer.repository;


import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.MemberQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
  @Query("SELECT a FROM Answer a LEFT JOIN FETCH a.member WHERE a.memberQuestion.id = :memberQuestionId")
  Optional<Answer> findByMemberQuestion_Id(UUID memberQuestionId);

  int deleteAllByMember(Member member);

  List<Answer> findAllByMember_Id(UUID memberId);

  @Modifying
  @Query("DELETE FROM Answer a WHERE a.memberQuestion.family.id = :familyId")
  void deleteByMemberQuestionFamilyId(@Param("familyId") UUID familyId);

  boolean existsByMemberQuestion_Id(UUID memberQuestionId);

  void deleteAllByFamily_Id(UUID familyId);
}