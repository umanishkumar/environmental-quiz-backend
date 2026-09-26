package com.environment.quiz.dto.quiz.ai;

import java.util.List;

public record AiQuizResponse(
        String title,
        String topic,
        String difficulty,
        List<AiQuestionResponse> questions
) {}