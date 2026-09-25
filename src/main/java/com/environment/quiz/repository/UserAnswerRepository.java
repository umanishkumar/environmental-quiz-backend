package com.environment.quiz.repository;

import com.environment.quiz.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByQuizAttemptId(Long quizAttemptId);
}