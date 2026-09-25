package com.environment.quiz.dto.quiz;

public record QuestionOptionResponse(
        Long id,
        String optionText
        // deliberately NO isCorrect field — never send the answer before submission
) {}