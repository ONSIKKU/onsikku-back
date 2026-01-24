package com.onsikku.onsikku_back.domain.answer.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.onsikku.onsikku_back.domain.answer.domain.AnswerType;
import com.onsikku.onsikku_back.domain.answer.domain.ReactionType;

import java.util.UUID;

public record AnswerRequest (
    UUID answerId,
    UUID questionAssignmentId,
    AnswerType answerType,
    JsonNode content,
    ReactionType reactionType
) {}