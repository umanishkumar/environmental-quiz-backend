package com.environment.quiz.service.quiz;

import com.environment.quiz.dto.quiz.QuizResponse;
import com.environment.quiz.entity.*;
import com.environment.quiz.exception.ResourceNotFoundException;
import com.environment.quiz.mapper.QuizMapper;
import com.environment.quiz.repository.*;
import com.environment.quiz.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final CurrentUserProvider currentUserProvider;

    private final QuizRepository quizRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final QuizMapper quizMapper;

    /**
     * Manual quiz creation for testing persistence before AI is wired in (Phase 12).
     * Mirrors the shape AI generation will eventually produce.
     */
    @Transactional
    public QuizResponse createManualQuiz(String topicName,
                                         Quiz.Difficulty difficulty,
                                         Quiz.QuestionType questionType,
                                         String language,
                                         List<ManualQuestionInput> questionInputs) {

        User user = currentUserProvider.getCurrentUser();


        Topic topic = topicRepository.findByNameIgnoreCase(topicName)
                .orElseGet(() -> topicRepository.save(
                        Topic.builder().name(topicName).predefined(false).build()));

        Quiz quiz = Quiz.builder()
                .title(topicName + " Quiz")
                .user(user)
                .topic(topic)
                .difficulty(difficulty)
                .questionType(questionType)
                .language(language)
                .numberOfQuestions(questionInputs.size())
                .build();

        for (ManualQuestionInput qi : questionInputs) {
            Question question = Question.builder()
                    .quiz(quiz)
                    .questionText(qi.questionText())
                    .explanation(qi.explanation())
                    .hint(qi.hint())
                    .build();

            for (int i = 0; i < qi.options().size(); i++) {
                QuestionOption option = QuestionOption.builder()
                        .question(question)
                        .optionText(qi.options().get(i))
                        .isCorrect(i == qi.correctIndex())
                        .build();
                question.getOptions().add(option);
            }
            quiz.getQuestions().add(question);
        }

        Quiz saved = quizRepository.save(quiz);
        return quizMapper.toQuizResponse(saved);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        return quizMapper.toQuizResponse(quiz);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(quizMapper::toQuizResponse)
                .toList();
    }
    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        quizRepository.delete(quiz);
    }

    public record ManualQuestionInput(
            String questionText,
            List<String> options,
            int correctIndex,
            String explanation,
            String hint
    ) {}
}