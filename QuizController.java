package com.quize.quizproject.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quize.quizproject.service.QuizService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    private static final Logger logger = LogManager.getLogger(QuestionController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();


    // Create or Update Quiz
    @CrossOrigin
    @PostMapping("/create")
    public ResponseEntity<JsonNode> createOrUpdateQuiz(@RequestBody JsonNode jsonNode) {
        logger.debug("Received request to process quiz data: {}", jsonNode.toString());

        try {
            // Call the service method to create or update the quiz
            JsonNode result = quizService.createQuiz(jsonNode);
            logger.debug("Quiz processed successfully: {}", result);
            return ResponseEntity.ok(result);
        } catch (DataAccessException dae) {
            logger.error("DataAccessException in processing quiz data: {}", dae.getMessage(), dae);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while processing quiz data"));
        } catch (Exception e) {
            logger.error("Exception in processing quiz data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while processing quiz data"));
        }
    }

    @CrossOrigin
    @PostMapping("/getQuizQues")
    public ResponseEntity<JsonNode> getQuizQuestion(@RequestBody JsonNode jsonNode) {
        // Expecting the request body to contain the quizId
        Integer quizId = jsonNode.get("quizId").asInt(); // Extract quizId from the JSON body
        logger.debug("Received request to get questions for quiz with ID: {}", quizId);

        try {
            // Call the service method to get quiz details and associated questions
            JsonNode result = quizService.getQuizQuestion(quizId);
            logger.debug("Quiz questions fetched successfully: {}", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error occurred while fetching quiz questions for quizId {}: {}", quizId, e.getMessage(), e);
            return ResponseEntity.status(500).body(createErrorResponse("Error occurred while fetching quiz questions"));
        }
    }

    @CrossOrigin
    @PostMapping("/quizSubmit")
    public ResponseEntity<JsonNode> processQuizSubmission(@RequestBody JsonNode submissionData) {
        try {
            // Validate request payload
            if (!submissionData.isArray() || submissionData.size() == 0) {
                ObjectNode errorResponse = new ObjectMapper().createObjectNode()
                        .put("error", "Invalid submission data. Expected a non-empty array.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Process the submission using service
            JsonNode response = quizService.processUserQuizSubmission(submissionData);

            // Return the result
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log error and return a 500 response
            ObjectNode errorResponse = new ObjectMapper().createObjectNode()
                    .put("error", "An unexpected error occurred while processing the quiz submission");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private ObjectNode createErrorResponse(String message) {
        ObjectNode errorResponse = objectMapper.createObjectNode();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        return errorResponse;
    }
}
