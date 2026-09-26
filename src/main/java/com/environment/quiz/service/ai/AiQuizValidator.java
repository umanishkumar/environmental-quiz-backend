package com.environment.quiz.service.ai;

import com.environment.quiz.dto.quiz.ai.AiQuestionResponse;
import com.environment.quiz.dto.quiz.ai.AiQuizResponse;
import com.environment.quiz.dto.quiz.ai.AiSingleQuestionResponse;
import com.environment.quiz.exception.InvalidAiOutputException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AiQuizValidator {

    private static final int REQUIRED_OPTION_COUNT = 4;
    private static final int MIN_QUESTION_LENGTH = 10;

    public void validate(AiQuizResponse response, int expectedQuestionCount) {
        if (response == null) {
            throw new InvalidAiOutputException("AI returned no response");
        }

        List<AiQuestionResponse> questions = response.questions();

        if (questions == null || questions.isEmpty()) {
            throw new InvalidAiOutputException("AI response contained no questions");
        }

        if (questions.size() != expectedQuestionCount) {
            throw new InvalidAiOutputException(
                    "Expected " + expectedQuestionCount + " questions but got " + questions.size());
        }

        Set<String> seenQuestionTexts = new HashSet<>();

        for (AiQuestionResponse q : questions) {
            validateSingleQuestion(q);

            String normalized = q.question().trim().toLowerCase();
            if (!seenQuestionTexts.add(normalized)) {
                throw new InvalidAiOutputException("Duplicate question detected: " + q.question());
            }
        }
    }

    private void validateSingleQuestion(AiQuestionResponse q) {
        if (q.question() == null || q.question().isBlank()) {
            throw new InvalidAiOutputException("A question has empty text");
        }

        if (q.question().trim().length() < MIN_QUESTION_LENGTH) {
            throw new InvalidAiOutputException("Question text is unreasonably short: " + q.question());
        }

        if (q.options() == null || q.options().size() != REQUIRED_OPTION_COUNT) {
            throw new InvalidAiOutputException(
                    "Question must have exactly " + REQUIRED_OPTION_COUNT + " options: " + q.question());
        }

        for (String option : q.options()) {
            if (option == null || option.isBlank()) {
                throw new InvalidAiOutputException("An option is empty in question: " + q.question());
            }
        }

        // Check for duplicate options within the same question
        Set<String> normalizedOptions = new HashSet<>();
        for (String option : q.options()) {
            normalizedOptions.add(option.trim().toLowerCase());
        }
        if (normalizedOptions.size() != q.options().size()) {
            throw new InvalidAiOutputException("Duplicate options found in question: " + q.question());
        }

        if (q.correctAnswer() == null || q.correctAnswer().isBlank()) {
            throw new InvalidAiOutputException("Missing correct answer for question: " + q.question());
        }

        boolean correctAnswerExistsInOptions = q.options().stream()
                .anyMatch(opt -> opt.trim().equalsIgnoreCase(q.correctAnswer().trim()));

        if (!correctAnswerExistsInOptions) {
            throw new InvalidAiOutputException(
                    "Correct answer does not match any option for question: " + q.question());
        }

        if (q.explanation() == null || q.explanation().isBlank()) {
            throw new InvalidAiOutputException("Missing explanation for question: " + q.question());
        }
        // hint is optional per Section 5 — deliberately not validated as required
    }
    public void validateSingleQuestionPublic(AiSingleQuestionResponse response) {
        if (response == null) {
            throw new InvalidAiOutputException("AI returned no response for question regeneration");
        }
        if (response.question() == null || response.question().isBlank()) {
            throw new InvalidAiOutputException("Regenerated question has empty text");
        }
        if (response.question().trim().length() < MIN_QUESTION_LENGTH) {
            throw new InvalidAiOutputException("Regenerated question text is unreasonably short");
        }
        if (response.options() == null || response.options().size() != REQUIRED_OPTION_COUNT) {
            throw new InvalidAiOutputException("Regenerated question must have exactly " + REQUIRED_OPTION_COUNT + " options");
        }
        for (String option : response.options()) {
            if (option == null || option.isBlank()) {
                throw new InvalidAiOutputException("An option is empty in regenerated question");
            }
        }
        if (response.correctAnswer() == null || response.correctAnswer().isBlank()) {
            throw new InvalidAiOutputException("Missing correct answer for regenerated question");
        }
        boolean correctAnswerExistsInOptions = response.options().stream()
                .anyMatch(opt -> opt.trim().equalsIgnoreCase(response.correctAnswer().trim()));
        if (!correctAnswerExistsInOptions) {
            throw new InvalidAiOutputException("Correct answer does not match any option in regenerated question");
        }
        if (response.explanation() == null || response.explanation().isBlank()) {
            throw new InvalidAiOutputException("Missing explanation for regenerated question");
        }
    }
}