package com.quize.quizproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quize.quizproject.model.Question;
import com.quize.quizproject.model.Quiz;
import com.quize.quizproject.repository.QuestionRepository;
import com.quize.quizproject.repository.QuizRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private static final Logger logger = LogManager.getLogger(QuestionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();



    @Transactional
    public JsonNode createQuiz(JsonNode jsonNode) {
        try {
            // Validate input
            if (!jsonNode.has("title") || !jsonNode.has("category") || !jsonNode.has("numQ")) {
                throw new IllegalArgumentException("Fields 'title', 'category', and 'numQ' are required.");
            }

            // Extract input values
            String category = jsonNode.get("category").asText();
            Integer numQ = jsonNode.get("numQ").asInt();
            String title = jsonNode.get("title").asText();

            // Fetch random questions based on category and count
            List<Question> questionList = questionRepository.findRandomQuestionByCategory(category, numQ);
            if (questionList.isEmpty()) {
                throw new IllegalArgumentException("No questions found for the specified category and count.");
            }

            // Initialize a Quiz object
            Quiz quiz;
            if (jsonNode.has("quizId") && !jsonNode.get("quizId").isNull()) {
                Integer quizId = jsonNode.get("quizId").asInt();
                quiz = quizRepository.findById(quizId).orElse(new Quiz());
                logger.debug(quiz.getQuizId() != null ?
                        "Updating existing quiz with ID: {}" : "Creating a new quiz.", quizId);
            } else {
                quiz = new Quiz();
                logger.debug("Creating a new quiz.");
            }

            // Set quiz fields
            quiz.setTitle(title);

            // Set the actual list of Question objects
            quiz.setQuestionList(questionList); // Store the full list of Question objects

            // Save the quiz to the database
            Quiz savedQuiz = quizRepository.save(quiz);
            logger.info("Quiz saved successfully with ID: {}", savedQuiz.getQuizId());

            // Prepare the response
            ObjectNode response = objectMapper.createObjectNode();
            response.put("quizId", savedQuiz.getQuizId());
            response.put("title", savedQuiz.getTitle());

            // Map the list of questionIds from the saved quiz (full questions list)
            List<Integer> questionIds = savedQuiz.getQuestionList().stream()
                    .map(Question::getQuestionId)
                    .collect(Collectors.toList());
            response.set("questionIds", objectMapper.convertValue(questionIds, JsonNode.class));

            return response;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input format: {}", e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating or updating quiz: {}", e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Error processing quiz data");
        }
    }

    @Transactional
    public JsonNode getQuizQuestion(Integer quizId) {
        try {
            // Fetch the quiz by quizId
            Quiz quiz = quizRepository.findById(quizId).orElse(null);

            if (quiz == null) {
                // If quiz is not found, return an error message
                logger.error("Quiz with ID {} not found", quizId);
                return objectMapper.createObjectNode().put("error", "Quiz not found");
            }

            // Fetch the associated questions from the quiz
            List<Question> questionList = quiz.getQuestionList();

            // Prepare the response
            ObjectNode response = objectMapper.createObjectNode();
            response.put("quizId", quiz.getQuizId());
            response.put("title", quiz.getTitle());

            // Prepare the list of question details
            ArrayNode questionsNode = objectMapper.createArrayNode();
            for (int i = 0; i < questionList.size(); i++) {
                Question question = questionList.get(i);
                ObjectNode questionNode = objectMapper.createObjectNode();
                questionNode.put("questionId", question.getQuestionId());
                questionNode.put("category", question.getCategory());
                questionNode.put("questionTitle", question.getQuestionTitle());
                questionNode.put("option1", question.getOption1());
                questionNode.put("option2", question.getOption2());
                questionNode.put("option3", question.getOption3());
                questionNode.put("option4", question.getOption4());

                // Add the question node to the questions array
                questionsNode.add(questionNode);
            }

            response.set("questions", questionsNode);

            // Return the response with quiz and associated questions
            return response;

        } catch (Exception e) {
            logger.error("Error retrieving quiz questions for quizId {}: {}", quizId, e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Error retrieving quiz questions");
        }
    }


    @Transactional
    public JsonNode processUserQuizSubmission(JsonNode jsonObject) {
        try {
            // Parse submitted answers
            List<Integer> questionIds = new ArrayList<>();
            Map<Integer, String> userResponses = new HashMap<>();

            for (JsonNode node : jsonObject) {
                int questionId = node.get("questionId").asInt();
                String response = node.get("response").asText(); // Convert all responses to String
                questionIds.add(questionId);
                userResponses.put(questionId, response);
            }

            // Fetch correct answers from the repository
            List<Object[]> results = questionRepository.findCorrectAnswersByQuestionIds(questionIds);

            // Map correct answers
            Map<Integer, String> correctAnswers = new HashMap<>();
            for (Object[] row : results) {
                Integer questionId = (Integer) row[0];
                String correctAnswer = (String) row[1];
                correctAnswers.put(questionId, correctAnswer);
            }

            // Compare user responses with correct answers
            int correctCount = 0;
            for (Map.Entry<Integer, String> entry : userResponses.entrySet()) {
                String userResponse = entry.getValue();
                String correctAnswer = correctAnswers.get(entry.getKey());
                if (userResponse != null && userResponse.equals(correctAnswer)) {
                    correctCount++;
                }
            }

            // Create response JSON
            ObjectNode response = objectMapper.createObjectNode();
            response.put("totalQuestions", questionIds.size());
            response.put("correctAnswers", correctCount);
            response.put("incorrectAnswers", questionIds.size() - correctCount);

            return response;

        } catch (Exception e) {
            logger.error("Error processing quiz submission: {}", e.getMessage(), e);
            return objectMapper.createObjectNode().put("error", "Error processing quiz submission");
        }
    }



}
