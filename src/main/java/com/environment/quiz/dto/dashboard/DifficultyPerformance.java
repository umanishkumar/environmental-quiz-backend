package com.environment.quiz.dto.dashboard;

public record DifficultyPerformance(
        String difficulty,
        long attemptCount,
        double averagePercentage
) {}