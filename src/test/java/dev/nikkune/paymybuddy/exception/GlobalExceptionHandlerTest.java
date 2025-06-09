package dev.nikkune.paymybuddy.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private FieldError fieldError;

    @Mock
    private ConstraintViolation<Object> constraintViolation;

    @Mock
    private jakarta.validation.Path path;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    @Test
    void handleRuntimeException_WithNotFoundMessage_ShouldReturnNotFoundStatus() {
        // Arrange
        RuntimeException exception = new RuntimeException("User not found");

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleRuntimeException(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertFalse((Boolean) responseEntity.getBody().get("success"));
        assertEquals("Not found", responseEntity.getBody().get("message"));
        assertEquals("User not found", responseEntity.getBody().get("error"));
    }

    @Test
    void handleRuntimeException_WithAlreadyExistsMessage_ShouldReturnConflictStatus() {
        // Arrange
        RuntimeException exception = new RuntimeException("User already exists");

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleRuntimeException(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertFalse((Boolean) responseEntity.getBody().get("success"));
        assertEquals("Conflict", responseEntity.getBody().get("message"));
        assertEquals("User already exists", responseEntity.getBody().get("error"));
    }

    @Test
    void handleRuntimeException_WithOtherMessage_ShouldReturnBadRequestStatus() {
        // Arrange
        RuntimeException exception = new RuntimeException("Some other error");

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleRuntimeException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertFalse((Boolean) responseEntity.getBody().get("success"));
        assertEquals("Bad request", responseEntity.getBody().get("message"));
        assertEquals("Some other error", responseEntity.getBody().get("error"));
    }

    @Test
    void handleValidationExceptions_MethodArgumentNotValid_ShouldReturnBadRequestWithErrors() {
        // Arrange
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(fieldError.getField()).thenReturn("username");
        when(fieldError.getDefaultMessage()).thenReturn("Username is required");
        when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertFalse((Boolean) responseEntity.getBody().get("success"));
        assertEquals("Bad request", responseEntity.getBody().get("message"));

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) responseEntity.getBody().get("errors");
        assertEquals("Username is required", errors.get("username"));
    }

    @Test
    void handleValidationExceptions_ConstraintViolation_ShouldReturnBadRequestWithErrors() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(constraintViolation);

        when(constraintViolationException.getConstraintViolations()).thenReturn(violations);
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("user.email");
        when(constraintViolation.getMessage()).thenReturn("Email is invalid");

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleValidationExceptions(constraintViolationException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertFalse((Boolean) responseEntity.getBody().get("success"));
        assertEquals("Bad request", responseEntity.getBody().get("message"));

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) responseEntity.getBody().get("errors");
        assertEquals("Email is invalid", errors.get("email"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerErrorStatus() {
        // Arrange
        Exception exception = new Exception("Unexpected error");

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleGenericException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertFalse((Boolean) responseEntity.getBody().get("success"));
        assertEquals("Internal server error", responseEntity.getBody().get("message"));
        assertEquals("Unexpected error", responseEntity.getBody().get("errors"));
    }
}
