package com.onsikku.onsikku_back.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class BlockRequest {
    private UUID blockedId; // 차단할 유저의 ID
}