package com.environment.quiz.service.quiz;

import com.environment.quiz.dto.leaderboard.LeaderboardEntry;
import com.environment.quiz.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final QuizAttemptRepository quizAttemptRepository;

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard() {
        return quizAttemptRepository.findLeaderboard();
    }
}