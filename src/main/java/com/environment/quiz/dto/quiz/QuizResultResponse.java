package com.environment.quiz.dto.quiz;

import java.util.List;

public record QuizResultResponse(
        Long attemptId,
        int score,
        int total,
        double percentage,
        int correct,
        int incorrect,
        Long timeTakenSeconds,
        List<QuestionResultResponse> questionResults,
        String performanceSummary // filled in Section 10 logic, later phase
) {}