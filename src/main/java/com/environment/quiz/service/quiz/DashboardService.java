package com.environment.quiz.service.quiz;

import com.environment.quiz.dto.dashboard.*;
import com.environment.quiz.entity.QuizAttempt;
import com.environment.quiz.entity.User;
import com.environment.quiz.entity.UserAnswer;
import com.environment.quiz.repository.QuizAttemptRepository;
import com.environment.quiz.repository.UserAnswerRepository;
import com.environment.quiz.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int RECENT_QUIZ_LIMIT = 5;

    private final QuizAttemptRepository quizAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        User user = currentUserProvider.getCurrentUser();

        long totalQuizzes = quizAttemptRepository.countByUser(user);

        Double avgPercentage = quizAttemptRepository.findAveragePercentageByUser(user);
        Double bestPercentage = quizAttemptRepository.findBestPercentageByUser(user);

        List<QuizAttempt> allAttempts = quizAttemptRepository.findByUser(user);

        int totalQuestionsAnswered = allAttempts.stream()
                .mapToInt(QuizAttempt::getTotalQuestions)
                .sum();

        int totalCorrectAnswers = allAttempts.stream()
                .mapToInt(QuizAttempt::getCorrectAnswers)
                .sum();

        List<RecentQuizItem> recentQuizzes = quizAttemptRepository
                .findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, RECENT_QUIZ_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(a -> new RecentQuizItem(
                        a.getId(),
                        a.getQuiz().getTitle(),
                        a.getQuiz().getTopic().getName(),
                        a.getPercentage(),
                        a.getCreatedAt()))
                .toList();

        List<TopicPerformance> topicPerformance = quizAttemptRepository.findTopicPerformanceByUser(user);
        List<DifficultyPerformance> difficultyPerformance = quizAttemptRepository.findDifficultyPerformanceByUser(user);

        return new DashboardResponse(
                (int) totalQuizzes,
                avgPercentage != null ? avgPercentage : 0.0,
                bestPercentage != null ? bestPercentage : 0.0,
                totalQuestionsAnswered,
                totalCorrectAnswers,
                recentQuizzes,
                topicPerformance,
                difficultyPerformance
        );
    }
}