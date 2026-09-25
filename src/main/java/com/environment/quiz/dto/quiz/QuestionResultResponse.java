package com.environment.quiz.dto.quiz;

public record QuestionResultResponse(
        Long questionId,
        String questionText,
        String selectedOptionText, // null if skipped
        String correctOptionText,
        boolean correct,
        String explanation
) {}