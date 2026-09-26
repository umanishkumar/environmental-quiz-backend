package com.environment.quiz.controller;

import com.environment.quiz.dto.quiz.ai.*;
import com.environment.quiz.service.ai.QuizAssistanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistanceController {

    private final QuizAssistanceService quizAssistanceService;

    @PostMapping("/hint")
    public ResponseEntity<HintResponse> getHint(@Valid @RequestBody HintRequest request) {
        String hint = quizAssistanceService.generateHint(request.questionId());
        return ResponseEntity.ok(new HintResponse(hint));
    }

    @PostMapping("/explain")
    public ResponseEntity<ExplainResponse> explain(@Valid @RequestBody ExplainRequest request) {
        String explanation = quizAssistanceService.generateExplanation(
                request.questionId(), request.selectedOptionId());
        return ResponseEntity.ok(new ExplainResponse(explanation));
    }
}