package com.environment.quiz.dto.quiz;

import java.time.LocalDateTime;

public record QuizHistoryItemResponse(
        Long attemptId,
        Long quizId,
        String title,
        String topic,
        String difficulty,
        double percentage,
        int score,
        int total,
        LocalDateTime attemptedAt
) {}