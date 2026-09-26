package com.environment.quiz.controller;

import com.environment.quiz.dto.quiz.QuizHistoryItemResponse;
import com.environment.quiz.dto.quiz.QuizResultResponse;
import com.environment.quiz.service.quiz.QuizHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizHistoryController {

    private final QuizHistoryService quizHistoryService;

    @GetMapping("/history")
    public ResponseEntity<Page<QuizHistoryItemResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(quizHistoryService.getHistory(page, size));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<QuizResultResponse> getAttemptDetail(@PathVariable Long attemptId) {
        return ResponseEntity.ok(quizHistoryService.getAttemptDetail(attemptId));
    }
}