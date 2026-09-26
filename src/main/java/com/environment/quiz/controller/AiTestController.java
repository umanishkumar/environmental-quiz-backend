package com.environment.quiz.controller;

import com.environment.quiz.dto.quiz.ai.AiQuizResponse;
import com.environment.quiz.service.ai.QuizAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiTestController {

    private final QuizAIService quizAIService;

    // TEMPORARY — deleted once real /generate endpoint (Phase 17) is wired up
    @GetMapping("/api/ai-test")
    public AiQuizResponse test(
            @RequestParam(defaultValue = "Climate Change") String topic,
            @RequestParam(defaultValue = "MEDIUM") String difficulty,
            @RequestParam(defaultValue = "3") int numberOfQuestions,
            @RequestParam(defaultValue = "MCQ") String questionType,
            @RequestParam(defaultValue = "English") String language) {

        return quizAIService.generateQuiz(topic, difficulty, numberOfQuestions, questionType, language);
    }
}