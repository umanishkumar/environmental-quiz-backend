package com.environment.quiz.controller;
import com.environment.quiz.dto.quiz.QuizResultResponse;
import com.environment.quiz.dto.quiz.QuizSubmissionRequest;
import com.environment.quiz.service.quiz.QuizSubmissionService;
import com.environment.quiz.dto.quiz.QuizResponse;
import com.environment.quiz.service.quiz.QuizService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Validated
public class QuizController {

    private final QuizService quizService;

    // TEMPORARY test endpoint — replaced by /api/quizzes/generate (AI) in Phase 12
    @PostMapping("/test-create")
    public ResponseEntity<QuizResponse> createTestQuiz(@Valid @RequestBody TestQuizRequest request) {
        QuizResponse response = quizService.createManualQuiz(
                request.topic(),
                request.difficulty(),
                request.questionType(),
                request.language(),
                request.questions()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @GetMapping
    public ResponseEntity<List<QuizResponse>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }
    private final QuizSubmissionService quizSubmissionService;

    @PostMapping("/{id}/submit")
    public ResponseEntity<QuizResultResponse> submitQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizSubmissionRequest request) {
        return ResponseEntity.ok(quizSubmissionService.submitQuiz(id, request));
    }

    public record TestQuizRequest(
            @NotBlank(message = "Topic is required")
            String topic,
            com.environment.quiz.entity.Quiz.Difficulty difficulty,
            com.environment.quiz.entity.Quiz.QuestionType questionType,
            String language,
            @jakarta.validation.constraints.NotEmpty(message = "Questions list cannot be empty")
            List<QuizService.ManualQuestionInput> questions
    ) {}
}