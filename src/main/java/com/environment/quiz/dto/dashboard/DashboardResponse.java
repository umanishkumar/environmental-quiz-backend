package com.environment.quiz.dto.dashboard;

import java.util.List;

public record DashboardResponse(
        int totalQuizzesAttempted,
        double averageScorePercentage,
        double bestScorePercentage,
        int totalQuestionsAnswered,
        int totalCorrectAnswers,
        List<RecentQuizItem> recentQuizzes,
        List<TopicPerformance> topicPerformance,
        List<DifficultyPerformance> difficultyPerformance
) {}