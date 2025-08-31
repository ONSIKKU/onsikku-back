package com.onsikku.onsikku_back.domain.question.dto;


import java.util.UUID;

public record QuestionRequest (UUID id, String content) {}