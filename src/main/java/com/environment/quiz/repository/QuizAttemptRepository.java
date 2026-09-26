package com.environment.quiz.repository;
import com.environment.quiz.dto.leaderboard.LeaderboardEntry;
import com.environment.quiz.entity.QuizAttempt;
import com.environment.quiz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.environment.quiz.dto.dashboard.TopicPerformance;
import com.environment.quiz.dto.dashboard.DifficultyPerformance;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Page<QuizAttempt> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<QuizAttempt> findByUser(User user);

    @Query("SELECT AVG(a.percentage) FROM QuizAttempt a WHERE a.user = :user")
    Double findAveragePercentageByUser(@Param("user") User user);

    @Query("SELECT MAX(a.percentage) FROM QuizAttempt a WHERE a.user = :user")
    Double findBestPercentageByUser(@Param("user") User user);
    long countByUser(User user);

    @Query("""
        SELECT new com.environment.quiz.dto.dashboard.TopicPerformance(
            a.quiz.topic.name, COUNT(a), AVG(a.percentage))
        FROM QuizAttempt a
        WHERE a.user = :user
        GROUP BY a.quiz.topic.name
        ORDER BY AVG(a.percentage) ASC
        """)
    List<TopicPerformance> findTopicPerformanceByUser(@Param("user") User user);

    @Query("""
        SELECT new com.environment.quiz.dto.dashboard.DifficultyPerformance(
            CAST(a.quiz.difficulty AS string), COUNT(a), AVG(a.percentage))
        FROM QuizAttempt a
        WHERE a.user = :user
        GROUP BY a.quiz.difficulty
        """)
    List<DifficultyPerformance> findDifficultyPerformanceByUser(@Param("user") User user);
    @Query("""
    SELECT new com.environment.quiz.dto.leaderboard.LeaderboardEntry(
        a.user.username, COUNT(a), AVG(a.percentage), MAX(a.percentage))
    FROM QuizAttempt a
    GROUP BY a.user.username
    ORDER BY AVG(a.percentage) DESC
    """)
    List<LeaderboardEntry> findLeaderboard();
}
