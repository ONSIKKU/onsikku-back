package com.onsikku.onsikku_back.domain.answer.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.member.domain.Family;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.domain.question.domain.QuestionAssignment;
import com.onsikku.onsikku_back.domain.question.domain.QuestionInstance;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Objects;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
@Table(name = "answer",
    indexes = {
        @Index(name = "idx_answer_family_created", columnList = "family_id, createdAt DESC")
    })
public class Answer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_assignment_id", nullable = false, unique = true)
    @JsonIgnore
    private QuestionAssignment questionAssignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_instance_id", nullable = false)
    @JsonIgnore
    private QuestionInstance questionInstance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    @JsonIgnore
    private Family family;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false)
    private AnswerType answerType;

    @Type(JsonBinaryType.class)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private JsonNode content;

    @Column(name = "like_reaction_count", nullable = false)
    private int likeReactionCount;
    @Column(name = "angry_reaction_count", nullable = false)
    private int angryReactionCount;
    @Column(name = "sad_reaction_count", nullable = false)
    private int sadReactionCount;
    @Column(name = "funny_reaction_count", nullable = false)
    private int funnyReactionCount;
    public void incrementLikeReaction() {
        this.likeReactionCount += 1;
    }
    public void incrementAngryReaction() {
        this.angryReactionCount += 1;
    }
    public void incrementSadReaction() {
        this.sadReactionCount += 1;
    }
    public void incrementFunnyReaction() {
        this.funnyReactionCount += 1;
    }
    public void decreaseLikeReaction() {
        if (this.likeReactionCount > 0) this.likeReactionCount -= 1;
    }
    public void decreaseAngryReaction() {
        if (this.angryReactionCount > 0) this.angryReactionCount -= 1;
    }
    public void decreaseSadReaction() {
        if (this.sadReactionCount > 0) this.sadReactionCount -= 1;
    }
    public void decreaseFunnyReaction() {
        if (this.funnyReactionCount > 0) this.funnyReactionCount -= 1;
    }
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

    public static Answer create(QuestionAssignment questionAssignment, Member member, AnswerType answerType, JsonNode content) {
        Answer answer = Answer.builder()
            .questionAssignment(questionAssignment)
            .questionInstance(questionAssignment.getQuestionInstance()) // 현재 트랜잭션이 활성화된 상태이므로, Hibernate는 프록시 객체 반환 (실제 DB 접근 X)
            .member(member)
            .answerType(answerType)
            .content(content)
            .family(member.getFamily())
            .likeReactionCount(0)
            .angryReactionCount(0)
            .sadReactionCount(0)
            .funnyReactionCount(0)
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