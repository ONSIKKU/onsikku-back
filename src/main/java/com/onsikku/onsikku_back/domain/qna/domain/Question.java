package com.onsikku.onsikku_back.domain.qna.domain;

import com.onsikku.onsikku_back.domain.qna.dto.QuestionRequest;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Question extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String content;

    @Builder
    public Question(String content) { // Builder 패턴 생성자
        this.content = content;
    }

    public static Question of(QuestionRequest request) {
        return Question.builder()
                .content(request.content())
                .build();
    }

    public void updateQuestion(QuestionRequest request) {
        this.content = request.content();
    }
}