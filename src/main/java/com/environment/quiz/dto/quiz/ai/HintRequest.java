package com.environment.quiz.dto.quiz.ai;

import jakarta.validation.constraints.NotNull;

public record HintRequest(
        @NotNull(message = "Question ID is required")
        Long questionId
) {}