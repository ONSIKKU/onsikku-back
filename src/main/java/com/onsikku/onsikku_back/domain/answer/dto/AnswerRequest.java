package com.onsikku.onsikku_back.domain.answer.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.answer.domain.AnswerType;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AnswerRequest (
    UUID answerId,
    UUID questionAssignmentId,
    AnswerType answerType,
    @NotBlank(message = "내용을 입력해주세요.")
    JsonNode content
) {}