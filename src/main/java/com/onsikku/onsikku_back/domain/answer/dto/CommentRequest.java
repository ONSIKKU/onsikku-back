package com.onsikku.onsikku_back.domain.answer.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CommentRequest(
    UUID questionInstanceId,
    UUID commentId,
    UUID parentCommentId,
    @NotBlank(message = "내용을 입력해주세요.")
    String content
) {}
