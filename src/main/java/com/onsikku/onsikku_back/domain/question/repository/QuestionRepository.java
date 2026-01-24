package com.onsikku.onsikku_back.domain.question.repository;

import com.onsikku.onsikku_back.domain.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, String> {
  // 주인공이 받지 않은 템플릿 중, 오늘 레벨에 맞는 질문을 랜덤으로 하나 가져옴
  @Query(value = """
        SELECT * FROM question q 
        WHERE q.level IN :levels 
        AND q.id NOT IN (
            SELECT mq.question_id FROM member_question mq 
            WHERE mq.member_id = :memberId AND mq.question_id IS NOT NULL
        )
        ORDER BY RANDOM() LIMIT 1
        """, nativeQuery = true)
  Optional<Question> findRandomTemplateForMember(UUID memberId, List<Integer> levels);
}
