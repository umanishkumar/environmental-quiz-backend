package com.environment.quiz.repository;

import com.environment.quiz.entity.Quiz;
import com.environment.quiz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}