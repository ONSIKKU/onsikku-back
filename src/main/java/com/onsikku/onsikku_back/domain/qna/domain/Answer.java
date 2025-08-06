package com.onsikku.onsikku_back.domain.qna.domain;


import com.onsikku.onsikku_back.domain.qna.dto.AnswerRequest;
import com.onsikku.onsikku_back.domain.member.domain.Member;
import com.onsikku.onsikku_back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Answer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // todo : mvp 이후 lazy로 변경
    //  주의사항 : lazy 관련 프록시 오류가 있으므로 responseDTO 생성 후 변경하기
    @ManyToOne//(fetch = FetchType.LAZY)
    // TODO : 나중에 AssignedQuestion으로 변경
    private Question question;

    private String content;

    @ManyToOne//(fetch = FetchType.LAZY)
    private Member member;


    public static Answer of(Question question, AnswerRequest answerRequest, Member member) {
        return Answer.builder()
                .question(question)
                .content(answerRequest.content())
                .member(member)
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }
}