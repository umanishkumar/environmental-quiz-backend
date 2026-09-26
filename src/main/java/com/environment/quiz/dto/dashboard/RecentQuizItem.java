package com.environment.quiz.dto.dashboard;

import java.time.LocalDateTime;

public record RecentQuizItem(
        Long attemptId,
        String title,
        String topic,
        double percentage,
        LocalDateTime attemptedAt
) {}