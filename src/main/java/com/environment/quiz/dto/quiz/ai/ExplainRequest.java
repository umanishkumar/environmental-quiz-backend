package com.environment.quiz.dto.quiz.ai;

import jakarta.validation.constraints.NotNull;

public record ExplainRequest(
        @NotNull(message = "Question ID is required")
        Long questionId,

        // The option ID the user selected; null if they skipped the question
        Long selectedOptionId
) {}