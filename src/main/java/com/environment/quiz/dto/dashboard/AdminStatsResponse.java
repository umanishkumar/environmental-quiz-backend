package com.environment.quiz.dto.dashboard;

public record AdminStatsResponse(
        long totalUsers,
        long totalQuizzes,
        long totalAttempts,
        long totalQuestions
) {}