package com.environment.quiz.dto.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuizSubmissionRequest(

        @NotEmpty(message = "Answers cannot be empty")
        @Valid
        List<SubmitAnswerRequest> answers,

        // optional; frontend can send elapsed seconds from its own timer
        Long timeTakenSeconds
) {}