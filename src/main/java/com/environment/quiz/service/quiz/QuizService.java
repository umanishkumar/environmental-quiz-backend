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
import com.environment.quiz.dto.quiz.QuizGenerationRequest;
import com.environment.quiz.dto.quiz.ai.AiQuestionResponse;
import com.environment.quiz.dto.quiz.ai.AiQuizResponse;
import com.environment.quiz.service.ai.QuizAIService;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final CurrentUserProvider currentUserProvider;
    private final QuizAIService quizAIService;
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
    @Transactional
    public QuizResponse generateAndSaveQuiz(QuizGenerationRequest request) {
        User user = currentUserProvider.getCurrentUser();

        Quiz.Difficulty difficulty = parseDifficulty(request.difficulty());
        Quiz.QuestionType questionType = parseQuestionType(request.questionType());
        String language = (request.language() == null || request.language().isBlank())
                ? "English" : request.language();

        AiQuizResponse aiResponse = quizAIService.generateQuiz(
                request.topic(),
                difficulty.name(),
                request.numberOfQuestions(),
                questionType.name(),
                language
        );

        Topic topic = topicRepository.findByNameIgnoreCase(request.topic())
                .orElseGet(() -> topicRepository.save(
                        Topic.builder().name(request.topic()).predefined(false).build()));

        Quiz quiz = Quiz.builder()
                .title(aiResponse.title() != null ? aiResponse.title() : request.topic() + " Quiz")
                .user(user)
                .topic(topic)
                .difficulty(difficulty)
                .questionType(questionType)
                .language(language)
                .numberOfQuestions(aiResponse.questions().size())
                .build();

        for (AiQuestionResponse aiQuestion : aiResponse.questions()) {
            Question question = Question.builder()
                    .quiz(quiz)
                    .questionText(aiQuestion.question())
                    .explanation(aiQuestion.explanation())
                    .hint(aiQuestion.hint())
                    .build();

            for (String optionText : aiQuestion.options()) {
                boolean isCorrect = optionText.trim().equalsIgnoreCase(aiQuestion.correctAnswer().trim());
                QuestionOption option = QuestionOption.builder()
                        .question(question)
                        .optionText(optionText)
                        .isCorrect(isCorrect)
                        .build();
                question.getOptions().add(option);
            }

            quiz.getQuestions().add(question);
        }

        Quiz saved = quizRepository.save(quiz);
        return quizMapper.toQuizResponse(saved);
    }

    private Quiz.Difficulty parseDifficulty(String difficulty) {
        try {
            return Quiz.Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("Invalid difficulty: " + difficulty + ". Must be EASY, MEDIUM, or HARD.");
        }
    }

    private Quiz.QuestionType parseQuestionType(String questionType) {
        try {
            return Quiz.QuestionType.valueOf(questionType.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("Invalid question type: " + questionType + ". Only MCQ is supported.");
        }
    }
    @Transactional
    public QuizResponse regenerateQuestion(Long quizId, Long questionId) {
        User user = currentUserProvider.getCurrentUser();

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        if (!quiz.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }

        Question questionToReplace = quiz.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question " + questionId + " not found in quiz " + quizId));

        List<String> existingQuestionTexts = quiz.getQuestions().stream()
                .filter(q -> !q.getId().equals(questionId))
                .map(Question::getQuestionText)
                .toList();

        var aiQuestion = quizAIService.regenerateSingleQuestion(
                quiz.getTopic().getName(),
                quiz.getDifficulty().name(),
                quiz.getQuestionType().name(),
                quiz.getLanguage(),
                existingQuestionTexts
        );

        // Replace the old question's content in place rather than swapping entity
        // identity — keeps the same Question row/id, simpler for any existing
        // UserAnswer rows that may already reference it.
        questionToReplace.setQuestionText(aiQuestion.question());
        questionToReplace.setExplanation(aiQuestion.explanation());
        questionToReplace.setHint(aiQuestion.hint());
        questionToReplace.getOptions().clear();

        for (String optionText : aiQuestion.options()) {
            boolean isCorrect = optionText.trim().equalsIgnoreCase(aiQuestion.correctAnswer().trim());
            QuestionOption option = QuestionOption.builder()
                    .question(questionToReplace)
                    .optionText(optionText)
                    .isCorrect(isCorrect)
                    .build();
            questionToReplace.getOptions().add(option);
        }

        Quiz saved = quizRepository.save(quiz);
        return quizMapper.toQuizResponse(saved);
    }
    @Transactional
    public QuizResponse regenerateQuiz(Long quizId) {
        User user = currentUserProvider.getCurrentUser();

        Quiz existingQuiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        if (!existingQuiz.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }

        QuizGenerationRequest request = new QuizGenerationRequest(
                existingQuiz.getTopic().getName(),
                existingQuiz.getDifficulty().name(),
                existingQuiz.getNumberOfQuestions(),
                existingQuiz.getQuestionType().name(),
                existingQuiz.getLanguage()
        );

        return generateAndSaveQuiz(request);
    }
}