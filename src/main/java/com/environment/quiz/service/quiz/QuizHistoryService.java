package com.environment.quiz.service.quiz;

import com.environment.quiz.dto.quiz.QuizHistoryItemResponse;
import com.environment.quiz.dto.quiz.QuizResultResponse;
import com.environment.quiz.dto.quiz.QuestionResultResponse;
import com.environment.quiz.entity.QuizAttempt;
import com.environment.quiz.entity.User;
import com.environment.quiz.entity.UserAnswer;
import com.environment.quiz.exception.ResourceNotFoundException;
import com.environment.quiz.mapper.QuizMapper;
import com.environment.quiz.repository.QuizAttemptRepository;
import com.environment.quiz.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizHistoryService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final CurrentUserProvider currentUserProvider;
    private final QuizMapper quizMapper;

    @Transactional(readOnly = true)
    public Page<QuizHistoryItemResponse> getHistory(int page, int size) {
        User user = currentUserProvider.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return quizAttemptRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(quizMapper::toHistoryItem);
    }

    @Transactional(readOnly = true)
    public QuizResultResponse getAttemptDetail(Long attemptId) {
        User user = currentUserProvider.getCurrentUser();

        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));

        // Never allow a user to view another user's private quiz history (Section 24)
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Attempt not found with id: " + attemptId);
        }

        var questionResults = attempt.getUserAnswers().stream()
                .map(this::toQuestionResult)
                .toList();

        return new QuizResultResponse(
                attempt.getId(),
                attempt.getCorrectAnswers(),
                attempt.getTotalQuestions(),
                attempt.getPercentage(),
                attempt.getCorrectAnswers(),
                attempt.getIncorrectAnswers(),
                attempt.getTimeTakenSeconds(),
                questionResults,
                buildPerformanceSummary(attempt.getPercentage())
        );
    }

    private QuestionResultResponse toQuestionResult(UserAnswer answer) {
        var correctOption = answer.getQuestion().getOptions().stream()
                .filter(o -> o.isCorrect())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Question " + answer.getQuestion().getId() + " has no correct option configured"));

        return new QuestionResultResponse(
                answer.getQuestion().getId(),
                answer.getQuestion().getQuestionText(),
                answer.getSelectedOption() != null ? answer.getSelectedOption().getOptionText() : null,
                correctOption.getOptionText(),
                answer.isCorrect(),
                answer.getQuestion().getExplanation()
        );
    }

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