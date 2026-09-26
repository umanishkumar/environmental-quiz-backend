package com.environment.quiz.service.ai;

import com.environment.quiz.entity.Question;
import com.environment.quiz.entity.QuestionOption;
import com.environment.quiz.exception.ResourceNotFoundException;
import com.environment.quiz.repository.QuestionOptionRepository;
import com.environment.quiz.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAssistanceService {

    private final ChatClient chatClient;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    public String generateHint(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        // If the AI already provided a hint at generation time, prefer that —
        // avoids an extra AI call for the common case.
        if (question.getHint() != null && !question.getHint().isBlank()) {
            return question.getHint();
        }

        String systemPrompt = """
                You are a helpful environmental education assistant.
                Provide a short hint (one sentence) for the given question that helps
                the learner think in the right direction WITHOUT revealing the correct
                answer or eliminating options directly.
                """;

        String userPrompt = """
                Question: %s
                Options: %s

                Provide a hint only. Do not state or imply which option is correct.
                """.formatted(question.getQuestionText(), formatOptions(question));

        String hint = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        log.info("Generated on-demand hint for question {}", questionId);
        return hint;
    }

    public String generateExplanation(Long questionId, Long selectedOptionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        QuestionOption correctOption = question.getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Question " + questionId + " has no correct option configured"));

        String selectedOptionText = null;
        if (selectedOptionId != null) {
            QuestionOption selectedOption = questionOptionRepository.findById(selectedOptionId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Option not found with id: " + selectedOptionId));
            selectedOptionText = selectedOption.getOptionText();
        }

        boolean wasCorrect = selectedOptionText != null
                && selectedOptionText.equalsIgnoreCase(correctOption.getOptionText());

        // If the user got it right, the stored explanation is usually sufficient —
        // avoid an unnecessary AI call.
        if (wasCorrect && question.getExplanation() != null && !question.getExplanation().isBlank()) {
            return question.getExplanation();
        }

        String systemPrompt = """
                You are a helpful environmental education assistant. Explain quiz answers
                clearly and concisely for a learner. Keep the explanation educational,
                factual, and encouraging — never condescending.
                """;

        String userPrompt = """
                Question: %s
                Correct answer: %s
                User's selected answer: %s

                Explain in 2-3 sentences:
                1. Why the correct answer is correct.
                2. If the user's answer was different, briefly why it's incorrect.
                Include relevant environmental context.
                """.formatted(
                question.getQuestionText(),
                correctOption.getOptionText(),
                selectedOptionText != null ? selectedOptionText : "(skipped)"
        );

        String explanation = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        log.info("Generated on-demand explanation for question {}", questionId);
        return explanation;
    }

    private String formatOptions(Question question) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (QuestionOption option : question.getOptions()) {
            sb.append(i++).append(". ").append(option.getOptionText()).append("\n");
        }
        return sb.toString();
    }
}