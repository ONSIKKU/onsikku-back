package com.onsikku.onsikku_back.domain.answer.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerRequest (
    Long id,
    Long questionId,
    @NotBlank(message = "내용을 입력해주세요.")
    String content

) {}