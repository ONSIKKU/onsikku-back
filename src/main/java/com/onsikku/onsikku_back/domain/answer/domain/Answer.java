package com.onsikku.onsikku_back.domain.answer.domain;


import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.Objects;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "answer")
    /*indexes = {
        @Index(name = "idx_answer_assignment", columnList = "assignment_id"),
        @Index(name = "idx_answer_type", columnList = "answer_type")
    })*/ // TODO : 인덱스? 는 보류
public class Answer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_assignment_id", nullable = false, unique = true)
    private QuestionAssignment questionAssignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false)
    private AnswerType answerType;

    @Type(JsonBinaryType.class)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private JsonNode content;

    public static Answer create(QuestionAssignment questionAssignment, Member member, AnswerType answerType, JsonNode content) {
        Answer answer = new Answer();
        answer.questionAssignment = questionAssignment;
        answer.member = member;
        answer.answerType = answerType;
        answer.content = content;

        answer.validate(); // 생성 시점에 유효성 검증
        return answer;
    }

    public void updateContent(JsonNode newContent) {
        this.content = newContent;
        this.validate(); // 수정 시점에도 유효성 검증
    }

    private void validate() {
        // 1. 답변 작성자가 할당된 사용자인지 확인
        if (!Objects.equals(this.questionAssignment.getMember().getId(), this.member.getId())) {
            throw new IllegalArgumentException("답변은 질문을 할당받은 사용자만 작성할 수 있습니다.");
        }

        // 2. 답변 타입에 따른 content 구조 확인
        if (this.answerType == null) this.answerType = AnswerType.TEXT;

        if (this.answerType == AnswerType.TEXT) {
            if (this.content == null || !this.content.has("text") || this.content.get("text").asText().isBlank()) {
                throw new IllegalArgumentException("TEXT 타입 답변은 'text' 필드가 비어있을 수 없습니다.");
            }
        }
        else if (answerType == AnswerType.MIXED) {
            boolean hasText = content != null && content.get("text") != null;
            boolean hasUrl = content != null && (content.get("url") != null || content.get("primary_url") != null);
            if (!(hasText && hasUrl)) throw new IllegalArgumentException("MIXED requires text + url");
        } else {
            if (content == null || content.get("url") == null) {
                throw new IllegalArgumentException("Media answer requires content.url");
            }
        }
        // author == assignment.recipient check
        if (questionAssignment != null && questionAssignment.getMember() != null && member != null) {
            if (!Objects.equals(questionAssignment.getMember().getId(), member.getId())) {
                throw new IllegalArgumentException("author must equal assignment recipient");
            }
        }
    }
}