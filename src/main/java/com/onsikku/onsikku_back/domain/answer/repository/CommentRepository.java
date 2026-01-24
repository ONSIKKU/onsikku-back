package com.onsikku.onsikku_back.domain.answer.repository;

import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
  @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.parentComment p JOIN FETCH c.member LEFT JOIN FETCH p.member " +
      "WHERE c.answer.id = :answerId " +
      "ORDER BY c.createdAt DESC")
  List<Comment> findAllByAnswerIdWithParentOrderByCreatedAtDesc(@Param("answerId") UUID answerId);

  int deleteAllByMember(Member member);

  @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.parentComment p JOIN FETCH c.member LEFT JOIN FETCH p.member WHERE c.id = :uuid")
  Optional<Comment> findByIdWithMemberAndParentAndParentMember(@Param("uuid") UUID uuid);

  @Query("SELECT c FROM Comment c JOIN FETCH c.member WHERE c.id = :uuid")
  Optional<Comment> findByIdWithMember(@Param("uuid") UUID uuid);

  @Modifying
  @Query("DELETE FROM Comment c WHERE c.answer.id IN " +
      "(SELECT a.id FROM Answer a WHERE a.memberQuestion.family.id = :familyId)")
  void deleteByAnswerMemberQuestionFamilyId(@Param("familyId") UUID familyId);
}
