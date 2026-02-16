package com.onsikku.onsikku_back.domain.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockedMemberResponse {
    private UUID blockedId;   // 차단된 사람의 ID
    private String nickname;  // 화면에 표시할 이름
    //private String profileImageUrl; // 추가 고려
}