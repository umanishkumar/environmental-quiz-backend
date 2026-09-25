package com.environment.quiz.dto.quiz;

import java.util.List;

public record QuestionResponse(
        Long id,
        String questionText,
        List<QuestionOptionResponse> options
        // no correctAnswer, no explanation, no hint here — those are revealed only
        // after submission (explanation) or on request (hint), per Sections 14 & 15
) {}