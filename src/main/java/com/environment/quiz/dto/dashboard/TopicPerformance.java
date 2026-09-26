package com.environment.quiz.dto.dashboard;

public record TopicPerformance(
        String topic,
        long attemptCount,
        double averagePercentage
) {}