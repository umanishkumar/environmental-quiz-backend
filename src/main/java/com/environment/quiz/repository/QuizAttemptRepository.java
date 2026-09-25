package com.environment.quiz.repository;

import com.environment.quiz.entity.QuizAttempt;
import com.environment.quiz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Page<QuizAttempt> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<QuizAttempt> findByUser(User user);

    @Query("SELECT AVG(a.percentage) FROM QuizAttempt a WHERE a.user = :user")
    Double findAveragePercentageByUser(@Param("user") User user);

    @Query("SELECT MAX(a.percentage) FROM QuizAttempt a WHERE a.user = :user")
    Double findBestPercentageByUser(@Param("user") User user);
}