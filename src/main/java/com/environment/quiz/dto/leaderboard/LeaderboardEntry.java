package com.environment.quiz.dto.leaderboard;

public record LeaderboardEntry(
        String username,
        long totalQuizzes,
        double averagePercentage,
        double bestPercentage
) {}