package com.environment.quiz.service.user;

import com.environment.quiz.dto.dashboard.AdminStatsResponse;
import com.environment.quiz.entity.Topic;
import com.environment.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                quizRepository.count(),
                quizAttemptRepository.count(),
                questionRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    @Transactional
    public Topic createTopic(String name, String description) {
        if (topicRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Topic already exists: " + name);
        }
        Topic topic = Topic.builder()
                .name(name)
                .description(description)
                .predefined(true)
                .build();
        return topicRepository.save(topic);
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new com.environment.quiz.exception.ResourceNotFoundException(
                        "Topic not found with id: " + topicId));
        topicRepository.delete(topic);
    }
}