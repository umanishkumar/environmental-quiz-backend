package com.environment.quiz.dto.quiz;

import java.time.LocalDateTime;
import java.util.List;

public record QuizResponse(
        Long id,
        String title,
        String topic,
        String difficulty,
        String questionType,
        int numberOfQuestions,
        List<QuestionResponse> questions,
        LocalDateTime createdAt
) {}