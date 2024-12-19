package com.quize.quizproject.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quize.quizproject.service.QuestionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    private static final Logger logger = LogManager.getLogger(QuestionController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @CrossOrigin
    @PostMapping("/ques")
    public ResponseEntity<JsonNode> createOrUpdateQuestion(@RequestBody JsonNode jsonNode) {
        logger.debug("Received request to process question data: {}", jsonNode.toString());

        try {
            // Call the service method and get the result
            JsonNode result = questionService.createOrUpdateQuestion(jsonNode);
            logger.debug("Question processed successfully: {}", result);
            return ResponseEntity.ok(result);
        } catch (DataAccessException dae) {
            logger.error("DataAccessException in process question data: {}", dae.getMessage(), dae);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while processing question data"));
        } catch (Exception e) {
            logger.error("Exception in process question data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while processing question data"));
        }
    }

    @CrossOrigin
    @PostMapping("/category")
    public ResponseEntity<JsonNode> getQuestionsByCategory(@RequestBody JsonNode jsonObject) {
        logger.debug("Received request to fetch questions by category: {}", jsonObject.toString());

        try {
            // Call the service method and get the result
            JsonNode result = questionService.getQuestionByCategory(jsonObject);
            logger.debug("Questions fetched successfully for the given category: {}", result);
            return ResponseEntity.ok(result);
        } catch (DataAccessException dae) {
            logger.error("DataAccessException in fetching questions by category: {}", dae.getMessage(), dae);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while fetching questions by category"));
        } catch (IllegalArgumentException iae) {
            logger.error("Invalid request: {}", iae.getMessage(), iae);
            return ResponseEntity.badRequest().body(createErrorResponse(iae.getMessage()));
        } catch (Exception e) {
            logger.error("Exception in fetching questions by category: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while fetching questions by category"));
        }
    }

    @CrossOrigin
    @GetMapping("/allQuestions")
    public ResponseEntity<JsonNode> getAllQuestion() {
        logger.debug("Received request to fetch all questions.");

        try {
            // Call the service method and get the result
            JsonNode result = questionService.getAllQuestion();
            logger.debug("Fetched all questions successfully.");
            return ResponseEntity.ok(result);
        } catch (DataAccessException dae) {
            logger.error("DataAccessException in fetching all questions: {}", dae.getMessage(), dae);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while fetching all questions."));
        } catch (Exception e) {
            logger.error("Exception in fetching all questions: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while fetching all questions."));
        }
    }


    @CrossOrigin
    @PostMapping("/delete")
    public ResponseEntity<JsonNode> deleteQuestion(@RequestBody JsonNode request) {
        logger.debug("Received request to delete question: {}", request.toString());

        try {
            // Extract questionId from the request body
            if (!request.has("questionId")) {
                logger.error("Missing questionId in the request body.");
                return ResponseEntity.badRequest().body(createErrorResponse("questionId is required."));
            }

            Integer questionId = request.get("questionId").asInt();

            // Call the service method to delete the question
            JsonNode result = questionService.deleteQuestion(questionId);
            logger.debug("Delete operation completed: {}", result);
            return ResponseEntity.ok(result);
        } catch (DataAccessException dae) {
            logger.error("DataAccessException in deleting question: {}", dae.getMessage(), dae);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while deleting the question."));
        } catch (Exception e) {
            logger.error("Exception in deleting question: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while deleting the question."));
        }
    }





    private ObjectNode createErrorResponse(String message) {
        ObjectNode errorResponse = objectMapper.createObjectNode();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        return errorResponse;
    }



}
