package com.quize.quizproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quize.quizproject.model.Question;
import com.quize.quizproject.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    private static final Logger logger = LogManager.getLogger(QuestionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

//    @Transactional
//    public JsonNode createOrUpdateQuestion(JsonNode jsonObject) {
//        ObjectNode response = objectMapper.createObjectNode();
//
//        try {
//            Question question = null;
//
//            // Check if Question ID is present in the JSON object
//            if (jsonObject.has("questionId")) {
//                Integer questionId = jsonObject.get("questionId").asInt();
//                question = questionRepository.findById(questionId).orElse(new Question());
//                logger.debug("Updating existing question with ID: {}", questionId);
//            } else {
//                question = new Question();
//                logger.debug("Creating a new question");
//            }
//
//            // Set values to the Question entity from JSON object
//          //  question.setQuestionId(jsonObject.has("questionId") ? jsonObject.get("questionId").asInt() : null);
//            question.setCategory(jsonObject.has("category") ? jsonObject.get("category").asText() : null);
//            question.setQuestionTitle(jsonObject.has("questionTitle") ? jsonObject.get("questionTitle").asText() : null);
//            question.setOption1(jsonObject.has("option1") ? jsonObject.get("option1").asText() : null);
//            question.setOption2(jsonObject.has("option2") ? jsonObject.get("option2").asText() : null);
//            question.setOption3(jsonObject.has("option3") ? jsonObject.get("option3").asText() : null);
//            question.setOption4(jsonObject.has("option4") ? jsonObject.get("option4").asText() : null);
//            question.setRightAns(jsonObject.has("rightAns") ? jsonObject.get("rightAns").asText() : null);
//            question.setDifficultyLevel(jsonObject.has("difficultyLevel") ? jsonObject.get("difficultyLevel").asText() : null);
//
//            // Save the Question entity
//            Question savedQuestion = questionRepository.save(question);
//            logger.debug("Question saved with ID: {}", savedQuestion.getQuestionId());
//
//
//            // Create response JSON with the result and the saved object
//            response.put("status","success");
//            response.put("result","question data created or updated successfully");
//            response.put("questionId",savedQuestion.getQuestionId());
//            response.put("category",savedQuestion.getCategory());
//            response.put("questionTitle",savedQuestion.getQuestionTitle());
//            response.put("option1",savedQuestion.getOption1());
//            response.put("option2",savedQuestion.getOption2());
//            response.put("option3",savedQuestion.getOption3());
//            response.put("option4",savedQuestion.getOption4());
//
//            return response;
//
//
//        } catch (Exception e) {
//            logger.error("Error during question processing: {}", e.getMessage(), e);
//            return objectMapper.createObjectNode()
//                    .put("result", "Error occurred while processing question data")
//                    .put("status", "error");
//        }
//    }

    @Transactional
    public JsonNode createOrUpdateQuestion(JsonNode jsonNode) {
        ArrayNode responseArray = objectMapper.createArrayNode(); // Array to hold response for each processed question

        try {
            // If the input is not an array, throw an error
            if (!jsonNode.isArray()) {
                return objectMapper.createObjectNode()
                        .put("status","error")
                        .put("result","json node array type not provided");
            }

            // Process each question in the array
            for (JsonNode questionObject : jsonNode) {
                Question question;

                // Check if question_id exists and is valid for updating an existing record
                if (questionObject.has("questionId") && !questionObject.get("questionId").isNull()) {
                    Integer questionId = questionObject.get("questionId").asInt();

                    // Fetch existing question by ID
                    question = questionRepository.findById(questionId).orElse(null);

                    if (question != null) {
                        logger.debug("Updating existing question with ID: {}", questionId);
                    } else {
                        logger.debug("No existing question found with ID: {}. Creating a new question.", questionId);
                        question = new Question(); // Create a new question if not found
                    }
                } else {
                    // If no question_id is provided, create a new question
                    logger.debug("No question_id provided. Creating a new question.");
                    question = new Question();
                }

                // Map fields for the question (whether new or updating)
                question.setCategory(questionObject.has("category") ? questionObject.get("category").asText() : null);
                question.setQuestionTitle(questionObject.has("questionTitle") ? questionObject.get("questionTitle").asText() : null);
                question.setOption1(questionObject.has("option1") ? questionObject.get("option1").asText() : null);
                question.setOption2(questionObject.has("option2") ? questionObject.get("option2").asText() : null);
                question.setOption3(questionObject.has("option3") ? questionObject.get("option3").asText() : null);
                question.setOption4(questionObject.has("option4") ? questionObject.get("option4").asText() : null);
                question.setRightAns(questionObject.has("rightAns") ? questionObject.get("rightAns").asText() : null);
                question.setDifficultyLevel(questionObject.has("difficultyLevel") ? questionObject.get("difficultyLevel").asText() : null);

                // Save or update the question to the database
                question = questionRepository.save(question);
                logger.debug("Question saved successfully");

                // Add the saved question to the response array
                ObjectNode savedQuestion = objectMapper.createObjectNode();
                savedQuestion.put("questionId", question.getQuestionId());
                savedQuestion.put("category", question.getCategory());
                savedQuestion.put("questionTitle", question.getQuestionTitle());
                savedQuestion.put("option1", question.getOption1());
                savedQuestion.put("option2", question.getOption2());
                savedQuestion.put("option3", question.getOption3());
                savedQuestion.put("option4", question.getOption4());
                savedQuestion.put("rightAns", question.getRightAns());
                savedQuestion.put("difficultyLevel", question.getDifficultyLevel());
                responseArray.add(savedQuestion);
            }

            // Return the processed questions as the response
            return responseArray;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input format: {}", e.getMessage(), e);
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Input must be an array of questions.");
            return errorResponse;
        } catch (Exception e) {
            logger.error("Error processing questions: {}", e.getMessage(), e);
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Error processing questions.");
            return errorResponse;
        }
    }


    @Transactional
    public JsonNode getQuestionByCategory(JsonNode jsonObject) {
        ObjectNode responseNode = objectMapper.createObjectNode();
        try {
            // Validate input
            if (!jsonObject.has("category") || jsonObject.get("category").isNull()) {
                throw new IllegalArgumentException("Category must be provided.");
            }

            String category = jsonObject.get("category").asText();

            // Fetch questions by category from the repository
            List<Question> questions = questionRepository.findByCategory(category);

            if (questions.isEmpty()) {
                responseNode.put("status", "error");
                responseNode.put("message", "No questions found for the specified category.");
            } else {
                // Convert List<Question> to JsonNode
                ArrayNode questionArray = objectMapper.createArrayNode();
                for (Question question : questions) {
                    ObjectNode questionNode = objectMapper.createObjectNode();
                    questionNode.put("questionId", question.getQuestionId());
                    questionNode.put("category", question.getCategory());
                    questionNode.put("questionTitle", question.getQuestionTitle());
                    questionNode.put("option1", question.getOption1());
                    questionNode.put("option2", question.getOption2());
                    questionNode.put("option3", question.getOption3());
                    questionNode.put("option4", question.getOption4());
                    questionNode.put("rightAns", question.getRightAns());
                    questionNode.put("difficultyLevel", question.getDifficultyLevel());
                    questionArray.add(questionNode);
                }

                responseNode.put("status", "success");
                responseNode.set("questions", questionArray);
            }
        } catch (IllegalArgumentException e) {
            responseNode.put("status", "error");
            responseNode.put("message", e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching questions by category: {}", e.getMessage(), e);
            responseNode.put("status", "error");
            responseNode.put("message", "An unexpected error occurred while fetching questions.");
        }
        return responseNode;
    }


    @Transactional
    public JsonNode getAllQuestion() {
        logger.debug("processing to get all question");
        ObjectNode response = objectMapper.createObjectNode();
        try {
            // Fetch all questions from the repository
            List<Question> questionList = questionRepository.findAll();

            // Manually build the JSON array response
            ArrayNode result = objectMapper.createArrayNode();
            for (Question question : questionList) {
                ObjectNode questionNode = objectMapper.createObjectNode();
                questionNode.put("questionId", question.getQuestionId());
                questionNode.put("category", question.getCategory());
                questionNode.put("questionTitle", question.getQuestionTitle());
                questionNode.put("option1", question.getOption1());
                questionNode.put("option2", question.getOption2());
                questionNode.put("option3", question.getOption3());
                questionNode.put("option4", question.getOption4());
                questionNode.put("rightAns", question.getRightAns());
                questionNode.put("difficultyLevel", question.getDifficultyLevel());
                result.add(questionNode);
            }

            logger.debug("Fetched all questions successfully. Total questions: {}", questionList.size());
            response.put("status","success");
            response.set("AllQuestions",result);
            return response;
        } catch (Exception e) {
            logger.error("Error occurred while fetching all questions: {}", e.getMessage(), e);
            return objectMapper.createObjectNode()
                            .put("status", "error")
                            .put("message", "Error occurred while fetching all questions.");
        }
    }


    @Transactional
    public JsonNode deleteQuestion(Integer questionId) {
        try {
            // Check if the question exists in the repository
            Optional<Question> questionOptional = questionRepository.findById(questionId);
            if (questionOptional.isPresent()) {
                // Delete the question
                questionRepository.deleteById(questionId);
                logger.debug("Question with ID {} deleted successfully.", questionId);

                // Return success response
                ObjectNode response = objectMapper.createObjectNode();
                response.put("status", "success");
                response.put("message", "Question deleted successfully.");
                response.put("questionId", questionId);
                return response;
            } else {
                // If question does not exist, return error response
                logger.warn("Question with ID {} not found.", questionId);
                ObjectNode response = objectMapper.createObjectNode();
                response.put("status", "error");
                response.put("message", "Question not found.");
                response.put("questionId", questionId);
                return response;
            }
        } catch (Exception e) {
            // Log and return generic error response
            logger.error("Error occurred while deleting question with ID {}: {}", questionId, e.getMessage(), e);
            ObjectNode response = objectMapper.createObjectNode();
            response.put("status", "error");
            response.put("message", "Error occurred while deleting the question.");
            return response;
        }
    }



}
