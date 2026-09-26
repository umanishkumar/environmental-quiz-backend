package com.environment.quiz.dto.quiz.ai;

import java.util.List;

public record AiQuestionResponse(
        String question,
        List<String> options,
        String correctAnswer,
        String explanation,
        String hint
) {}