package com.onsikku.onsikku_back.domain.answer.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.MemberQuestion;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "answer",
    indexes = {
        //@Index(name = "idx_answer_family_created", columnList = "family_id, createdAt DESC")
    })
public class Answer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)    // 확장성 고려하여 ManyToOne + unique 로 설정
    @JoinColumn(name = "member_question_id", nullable = false, unique = true)
    @JsonIgnore
    private MemberQuestion memberQuestion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false)
    private AnswerType answerType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private JsonNode content;

   public String extractTextContent() {
        if (this.content == null) {
            return "";
        }
        // .asText()는 NullNode(키가 없을 때)에도 예외 없이 빈 문자열을 반환합니다.
        return this.content.path("text").asText();
    }

    public String extractUrlContent() {
        if (this.content == null) {
            return null; // URL이 없으면 null 반환
        }
        // content 내에 'url' 또는 'primary_url' 키가 있을 수 있다고 가정하고 처리 가능
        if (this.content.has("url")) {
            return this.content.path("url").asText();
        }
        return null;
    }

    public static Answer create(MemberQuestion memberQuestion, Member member, AnswerType answerType, JsonNode content) {
        Answer answer = Answer.builder()
            .memberQuestion(memberQuestion)
            .member(member)
            .answerType(answerType)
            .content(content)
            .build();
        answer.validate(); // 생성 시점에 유효성 검증
        return answer;
    }

    public void updateContent(JsonNode newContent) {
        this.content = newContent;
        this.validate(); // 수정 시점에도 유효성 검증
    }

    private void validate() {
        // 1. 답변 작성자가 할당된 사용자인지 확인
        if (memberQuestion != null && memberQuestion.getMember() != null && member != null) {
            if (!Objects.equals(this.memberQuestion.getMember().getId(), this.member.getId())) {
                throw new IllegalArgumentException("답변은 질문을 할당받은 사용자만 작성할 수 있습니다.");
            }
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
    }
}