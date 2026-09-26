package com.environment.quiz.mapper;

import com.environment.quiz.dto.quiz.QuestionOptionResponse;
import com.environment.quiz.dto.quiz.QuestionResponse;
import com.environment.quiz.dto.quiz.QuizResponse;
import com.environment.quiz.entity.Quiz;
import org.springframework.stereotype.Component;
import com.environment.quiz.dto.quiz.QuizHistoryItemResponse;
import com.environment.quiz.entity.QuizAttempt;
import java.util.List;

@Component
public class QuizMapper {

    public QuizResponse toQuizResponse(Quiz quiz) {
        List<QuestionResponse> questionResponses = quiz.getQuestions().stream()
                .map(q -> new QuestionResponse(
                        q.getId(),
                        q.getQuestionText(),
                        q.getOptions().stream()
                                .map(o -> new QuestionOptionResponse(o.getId(), o.getOptionText()))
                                .toList()
                ))
                .toList();

        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTopic().getName(),
                quiz.getDifficulty().name(),
                quiz.getQuestionType().name(),
                quiz.getNumberOfQuestions(),
                questionResponses,
                quiz.getCreatedAt()
        );
    }
    public QuizHistoryItemResponse toHistoryItem(QuizAttempt attempt) {
        return new QuizHistoryItemResponse(
                attempt.getId(),
                attempt.getQuiz().getId(),
                attempt.getQuiz().getTitle(),
                attempt.getQuiz().getTopic().getName(),
                attempt.getQuiz().getDifficulty().name(),
                attempt.getPercentage(),
                attempt.getCorrectAnswers(),
                attempt.getTotalQuestions(),
                attempt.getCreatedAt()
        );
    }
}