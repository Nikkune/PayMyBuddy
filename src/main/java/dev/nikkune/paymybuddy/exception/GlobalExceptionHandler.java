package dev.nikkune.paymybuddy.exception;

import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles RuntimeExceptions that are thrown by the controllers.
     * <p>
     * If the exception message contains "not found", a 404 Not Found status is returned.
     * If the exception message contains "already exists", a 409 Conflict status is returned.
     * Otherwise, a 400 Bad Request status is returned.
     *
     * @param e the RuntimeException to handle
     * @return a ResponseEntity with the appropriate status and the exception message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        logger.error(e.getMessage());

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", e.getMessage());

        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
            response.put("message", "Not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (e.getMessage() != null && e.getMessage().toLowerCase().contains("already exists")) {
            response.put("message", "Conflict");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        response.put("message", "Bad request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle MethodArgumentNotValidException exceptions by returning a
     * ResponseEntity containing a map of errors.
     * <p>
     * The map contains the field names as the keys and the error messages as the
     * values.
     * <p>
     * The response will be a 400 Bad Request response, which indicates that the
     * request was invalid.
     *
     * @param e the exception to be handled
     * @return a ResponseEntity containing a map of errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.error(errors);

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Bad request");
        response.put("errors", errors);


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(error -> {
            String fieldName = error.getPropertyPath().toString().split("\\.")[(error.getPropertyPath().toString().split("\\.").length) - 1];
            String errorMessage = error.getMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.error(errors);

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Bad request");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle any uncaught exceptions by returning a generic 500 error response
     * with a message containing the exception's message.
     *
     * @param e the exception to be handled
     * @return a ResponseEntity containing the error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        logger.error("Internal server error: {}", e.getMessage());

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Internal server error");
        response.put("errors", e.getMessage());


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}