package com.onsikku.onsikku_back.domain.qna.dto;


import com.onsikku.onsikku_back.domain.member.domain.FamilyRole;
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
    private final FamilyRole familyRole;

    public static AnswerResponse from(Answer answer) {
        return AnswerResponse.builder()
                .familyRole(answer.getMember().getFamilyRole())
                .content(answer.getContent())
                .build();
    }
}
