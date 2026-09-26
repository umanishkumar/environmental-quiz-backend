package com.environment.quiz.service.ai;

import com.environment.quiz.dto.quiz.ai.AiQuizResponse;
import com.environment.quiz.exception.InvalidAiOutputException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAIService {

    private static final int MAX_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final PromptService promptService;
    private final AiQuizValidator aiQuizValidator;

    public AiQuizResponse generateQuiz(String topic, String difficulty, int numberOfQuestions,
                                       String questionType, String language) {

        String systemPrompt = promptService.buildSystemPrompt();
        String userPrompt = promptService.buildUserPrompt(topic, difficulty, numberOfQuestions, questionType, language);

        InvalidAiOutputException lastFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            long start = System.currentTimeMillis();

            AiQuizResponse response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(AiQuizResponse.class);

            long durationMs = System.currentTimeMillis() - start;

            try {
                aiQuizValidator.validate(response, numberOfQuestions);
                log.info("AI quiz generation succeeded on attempt {} in {} ms for topic '{}'",
                        attempt, durationMs, topic);
                return response;
            } catch (InvalidAiOutputException ex) {
                lastFailure = ex;
                log.warn("AI quiz generation attempt {} failed validation for topic '{}': {}",
                        attempt, topic, ex.getMessage());
            }
        }

        log.error("AI quiz generation failed after {} attempts for topic '{}'", MAX_ATTEMPTS, topic);
        throw new InvalidAiOutputException(
                "AI failed to generate a valid quiz after " + MAX_ATTEMPTS + " attempts. Last error: "
                        + (lastFailure != null ? lastFailure.getMessage() : "unknown"));
    }
}