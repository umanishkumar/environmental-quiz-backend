package com.environment.quiz.dto.quiz;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizGenerationRequest(

        @NotBlank(message = "Topic is required")
        String topic,

        @NotNull(message = "Difficulty is required")
        String difficulty, // EASY, MEDIUM, HARD — validated against enum in the service layer

        @Min(value = 1, message = "Number of questions must be at least 1")
        @Max(value = 20, message = "Number of questions cannot exceed 20")
        int numberOfQuestions,

        @NotBlank(message = "Question type is required")
        String questionType, // currently only MCQ

        String language // optional, defaults to "English" in service if blank
) {}