package com.onsikku.onsikku_back.domain.answer.dto;

import com.onsikku.onsikku_back.domain.answer.domain.ReactionType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReactionRequest(
    @NotNull(message = "답변 ID는 필수입니다.")
    UUID answerId,

    @NotNull(message = "리액션 타입은 필수입니다.")
    ReactionType type
) {}