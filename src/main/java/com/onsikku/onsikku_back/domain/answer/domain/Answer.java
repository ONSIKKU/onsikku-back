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
@Table(name = "answer",
    indexes = {
        @Index(name = "idx_answer_assignment", columnList = "assignment_id"),
        @Index(name = "idx_answer_type", columnList = "answer_type")
    })
public class Answer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private QuestionAssignment assignment;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;


    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false)
    private AnswerType answerType;


    @Type(JsonBinaryType.class)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private JsonNode content;


    @PrePersist
    void prePersist() {
        if (answerType == null) answerType = AnswerType.TEXT;
        // soft validation (DB-level stricter in Flyway SQL):
        if (answerType == AnswerType.TEXT) {
            if (content == null || content.get("text") == null) {
                throw new IllegalArgumentException("TEXT answer requires content.text");
            }
        } else if (answerType == AnswerType.MIXED) {
            boolean hasText = content != null && content.get("text") != null;
            boolean hasUrl = content != null && (content.get("url") != null || content.get("primary_url") != null);
            if (!(hasText && hasUrl)) throw new IllegalArgumentException("MIXED requires text + url");
        } else {
            if (content == null || content.get("url") == null) {
                throw new IllegalArgumentException("Media answer requires content.url");
            }
        }
        // author == assignment.recipient check
        if (assignment != null && assignment.getRecipient() != null && author != null) {
            if (!Objects.equals(assignment.getRecipient().getId(), author.getId())) {
                throw new IllegalArgumentException("author must equal assignment recipient");
            }
        }
    }
}