package com.environment.quiz.service.quiz;

import com.environment.quiz.dto.quiz.*;
import com.environment.quiz.entity.*;
import com.environment.quiz.exception.ResourceNotFoundException;
import com.environment.quiz.repository.*;
import com.environment.quiz.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizSubmissionService {

    private final CurrentUserProvider currentUserProvider;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Transactional
    public QuizResultResponse submitQuiz(Long quizId, QuizSubmissionRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        User user = currentUserProvider.getCurrentUser();

        // Index questions by id for quick lookup
        Map<Long, Question> questionById = new HashMap<>();
        for (Question q : quiz.getQuestions()) {
            questionById.put(q.getId(), q);
        }

        int correctCount = 0;
        int total = quiz.getQuestions().size();

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .totalQuestions(total)
                .timeTakenSeconds(request.timeTakenSeconds())
                .build();

        List<QuestionResultResponse> questionResults = new java.util.ArrayList<>();

        for (SubmitAnswerRequest answer : request.answers()) {
            Question question = questionById.get(answer.questionId());
            if (question == null) {
                throw new IllegalArgumentException(
                        "Question id " + answer.questionId() + " does not belong to quiz " + quizId);
            }

            QuestionOption correctOption = question.getOptions().stream()
                    .filter(QuestionOption::isCorrect)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Question " + question.getId() + " has no correct option configured"));

            QuestionOption selectedOption = null;
            boolean isCorrect = false;

            if (answer.selectedOptionId() != null) {
                selectedOption = questionOptionRepository.findById(answer.selectedOptionId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Option not found with id: " + answer.selectedOptionId()));
                isCorrect = selectedOption.getId().equals(correctOption.getId());
            }

            if (isCorrect) correctCount++;

            UserAnswer userAnswer = UserAnswer.builder()
                    .quizAttempt(attempt)
                    .question(question)
                    .selectedOption(selectedOption)
                    .correct(isCorrect)
                    .build();
            attempt.getUserAnswers().add(userAnswer);

            questionResults.add(new QuestionResultResponse(
                    question.getId(),
                    question.getQuestionText(),
                    selectedOption != null ? selectedOption.getOptionText() : null,
                    correctOption.getOptionText(),
                    isCorrect,
                    question.getExplanation()
            ));
        }

        int incorrectCount = total - correctCount;
        double percentage = total == 0 ? 0.0 : (correctCount * 100.0) / total;

        attempt.setCorrectAnswers(correctCount);
        attempt.setIncorrectAnswers(incorrectCount);
        attempt.setPercentage(percentage);

        QuizAttempt saved = quizAttemptRepository.save(attempt);

        return new QuizResultResponse(
                saved.getId(),
                correctCount,
                total,
                percentage,
                correctCount,
                incorrectCount,
                request.timeTakenSeconds(),
                questionResults,
                buildPerformanceSummary(percentage)
        );
    }

    // Simple, honest summary — no unsupported claims (Section 10)
    private String buildPerformanceSummary(double percentage) {
        if (percentage >= 80) {
            return "Strong performance overall.";
        } else if (percentage >= 50) {
            return "Solid attempt, with room to improve in some areas.";
        } else {
            return "This topic needs more review — consider trying again.";
        }
    }
}