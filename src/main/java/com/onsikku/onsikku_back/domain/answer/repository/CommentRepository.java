package com.onsikku.onsikku_back.domain.answer.repository;

import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
  List<Comment> findAllByQuestionInstance_IdOrderByCreatedAtDesc(UUID questionInstanceId);

  List<Comment> findAllByQuestionInstance_IdInOrderByCreatedAtDesc(List<UUID> instanceIds);

  void deleteByMember(Member member);

  int deleteAllByQuestionInstanceIn(List<QuestionInstance> questionInstances);
}
