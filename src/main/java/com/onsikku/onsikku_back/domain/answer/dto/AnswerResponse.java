package com.onsikku.onsikku_back.domain.answer.dto;


import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.member.domain.FamilyRole;
import com.onsikku.onsikku_back.domain.answer.domain.Answer;
import com.onsikku.onsikku_back.domain.member.domain.Gender;
import jakarta.persistence.Column;
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
    private Gender gender;
    // TODO : EC2에서 로컬데이트타임 변환 문제 해결 필요
    private LocalDateTime createdAt;
    private JsonNode content;
    private UUID answerId;
    private int likeReactionCount;
    private int angryReactionCount;
    private int sadReactionCount;
    private int funnyReactionCount;

    public static AnswerResponse from(Answer answer) {
        return AnswerResponse.builder()
            .memberId(answer.getMember().getId())
            .familyRole(answer.getMember().getFamilyRole())
            .gender(answer.getMember().getGender())
            .createdAt(answer.getCreatedAt())
            //TODO : answer jsonnode -> String 변환
            .content(answer.getContent())
            .answerId(answer.getId())
            .likeReactionCount(answer.getLikeReactionCount())
            .angryReactionCount(answer.getAngryReactionCount())
            .sadReactionCount(answer.getSadReactionCount())
            .funnyReactionCount(answer.getFunnyReactionCount())
            .build();
    }
}
