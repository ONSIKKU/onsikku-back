package com.onsikku.onsikku_back.domain.answer.dto;


import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.member.domain.FamilyRole;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerResponse {
    private UUID memberId;
    private FamilyRole familyRole;
    private String nickname;
    private JsonNode content;
    private UUID answerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnswerResponse from(Answer answer) {
        return AnswerResponse.builder()
            .memberId(answer.getMember().getId())
            .familyRole(answer.getMember().getFamilyRole())
            //TODO : answer jsonnode -> String 변환
            .content(answer.getContent())
            .answerId(answer.getId())
            .createdAt(answer.getCreatedAt())
            .updatedAt(answer.getUpdatedAt())
            .build();
    }
}
