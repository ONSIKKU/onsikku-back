package com.onsikku.onsikku_back.domain.qna.dto;


import com.onsikku.onsikku_back.domain.qna.domain.Answer;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class AnswerResponse {
    private final Long id;
    private final String content;
    private final String relation;

    public static AnswerResponse from(Answer answer) {
        return AnswerResponse.builder()
                .relation(answer.getMember().getRelation())
                .content(answer.getContent())
                .build();
    }
}
