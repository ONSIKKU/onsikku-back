package com.onsikku.onsikku_back.domain.answer.repository;

import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
  @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.parent p JOIN FETCH c.member LEFT JOIN FETCH p.member " +
      "WHERE c.questionInstance.id = :instanceId " +
      "ORDER BY c.createdAt DESC")
  List<Comment> findAllByQuestionInstanceIdWithParentOrderByCreatedAtDesc(@Param("instanceId") UUID questionInstanceId);

  int deleteAllByMember(Member member);

  int deleteAllByQuestionInstanceIn(List<QuestionInstance> questionInstances);

  List<Comment> findByParent(Comment parent);

  @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.parent p JOIN FETCH c.member LEFT JOIN FETCH p.member WHERE c.id = :uuid")
  Optional<Comment> findByIdWithParent(@Param("uuid") UUID uuid);
}
