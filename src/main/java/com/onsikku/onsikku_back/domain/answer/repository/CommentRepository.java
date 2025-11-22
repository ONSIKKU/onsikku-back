package com.onsikku.onsikku_back.domain.answer.repository;

import com.onsikku.onsikku_back.domain.answer.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
  List<Comment> findAllByQuestionInstanceId(UUID questionInstanceId);

  List<Comment> findAllByQuestionInstance_IdIn(List<UUID> instanceIds);
}
