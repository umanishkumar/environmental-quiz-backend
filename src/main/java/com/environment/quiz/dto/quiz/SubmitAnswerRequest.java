package com.environment.quiz.dto.quiz;

import jakarta.validation.constraints.NotNull;

public record SubmitAnswerRequest(

        @NotNull(message = "Question ID is required")
        Long questionId,

        // null means the user skipped this question
        Long selectedOptionId
) {}