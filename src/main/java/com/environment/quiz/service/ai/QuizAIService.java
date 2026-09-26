package com.environment.quiz.service.ai;

import com.environment.quiz.dto.quiz.ai.AiQuizResponse;
import com.environment.quiz.dto.quiz.ai.AiSingleQuestionResponse;
import com.environment.quiz.exception.InvalidAiOutputException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAIService {

    private static final int MAX_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final PromptService promptService;
    private final AiQuizValidator aiQuizValidator;
    private final EnvironmentalKnowledgeRetriever knowledgeRetriever;

    public AiQuizResponse generateQuiz(String topic, String difficulty, int numberOfQuestions,
                                       String questionType, String language) {

        // RAG: pull relevant grounding context for this topic before building the prompt.
        // Returns "" (never throws) if nothing relevant is found or retrieval fails.
        String retrievedContext = knowledgeRetriever.retrieveContext(topic);

        String systemPrompt = promptService.buildSystemPrompt();
        String userPrompt = promptService.buildUserPrompt(
                topic, difficulty, numberOfQuestions, questionType, language, retrievedContext);

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
                log.info("AI quiz generation succeeded on attempt {} in {} ms for topic '{}' (RAG context used: {})",
                        attempt, durationMs, topic, !retrievedContext.isBlank());
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
    public AiSingleQuestionResponse regenerateSingleQuestion(String topic, String difficulty,
                                                             String questionType, String language,
                                                             List<String> existingQuestionTexts) {

        String retrievedContext = knowledgeRetriever.retrieveContext(topic);
        String systemPrompt = promptService.buildSystemPrompt();
        String userPrompt = promptService.buildSingleQuestionPrompt(
                topic, difficulty, questionType, language, existingQuestionTexts, retrievedContext);

        InvalidAiOutputException lastFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            AiSingleQuestionResponse response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(AiSingleQuestionResponse.class);

            try {
                aiQuizValidator.validateSingleQuestionPublic(response);
                log.info("Regenerated single question succeeded on attempt {} for topic '{}'", attempt, topic);
                return response;
            } catch (InvalidAiOutputException ex) {
                lastFailure = ex;
                log.warn("Question regeneration attempt {} failed validation: {}", attempt, ex.getMessage());
            }
        }

        throw new InvalidAiOutputException(
                "AI failed to regenerate a valid question after " + MAX_ATTEMPTS + " attempts. Last error: "
                        + (lastFailure != null ? lastFailure.getMessage() : "unknown"));
    }
}