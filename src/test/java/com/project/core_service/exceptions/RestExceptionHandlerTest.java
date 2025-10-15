package com.project.core_service.exceptions;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestExceptionHandler Tests")
class RestExceptionHandlerTest {

    @InjectMocks
    private RestExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Nested
    @DisplayName("IllegalArgumentException Tests")
    class IllegalArgumentExceptionTests {

        @Test
        @DisplayName("Should handle IllegalArgumentException with message")
        @SuppressWarnings("unchecked")
        void shouldHandleIllegalArgumentExceptionWithMessage() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter value");
            when(webRequest.getDescription(false)).thenReturn("uri=/test/path");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(exception, webRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.BAD_REQUEST, body.get("status"));
            assertEquals("Invalid parameter value", body.get("message"));
            assertEquals("uri=/test/path", body.get("path"));
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException with null message")
        @SuppressWarnings("unchecked")
        void shouldHandleIllegalArgumentExceptionWithNullMessage() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException();
            when(webRequest.getDescription(false)).thenReturn("uri=/test/path");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(exception, webRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Invalid argument", body.get("message"));
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException Tests")
    class MethodArgumentNotValidExceptionTests {

        @Mock
        private MethodArgumentNotValidException methodArgumentNotValidException;

        @Mock
        private BindingResult bindingResult;

        @Test
        @DisplayName("Should handle validation errors with multiple messages")
        @SuppressWarnings("unchecked")
        void shouldHandleValidationErrorsWithMultipleMessages() {
            // Arrange
            ObjectError error1 = new FieldError("objectName", "field1", "Field1 is required");
            ObjectError error2 = new FieldError("objectName", "field2", "Field2 must be positive");
            List<ObjectError> errors = Arrays.asList(error1, error2);

            when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(errors);
            when(webRequest.getDescription(false)).thenReturn("uri=/test/path");

            HttpHeaders headers = new HttpHeaders();

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                    methodArgumentNotValidException, headers, HttpStatus.BAD_REQUEST, webRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(400, body.get("status"));
            assertEquals("Field1 is required; Field2 must be positive", body.get("message"));
            assertEquals("uri=/test/path", body.get("path"));
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Should handle validation errors with empty messages")
        @SuppressWarnings("unchecked")
        void shouldHandleValidationErrorsWithEmptyMessages() {
            // Arrange
            ObjectError error1 = new FieldError("objectName", "field1", "");
            ObjectError error2 = new FieldError("objectName", "field2", "   ");
            List<ObjectError> errors = Arrays.asList(error1, error2);

            when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(errors);
            when(webRequest.getDescription(false)).thenReturn("uri=/test/path");

            HttpHeaders headers = new HttpHeaders();

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                    methodArgumentNotValidException, headers, HttpStatus.BAD_REQUEST, webRequest);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Validation failed", body.get("message"));
        }

        @Test
        @DisplayName("Should handle validation errors with null messages")
        @SuppressWarnings({ "unchecked", "null" })
        void shouldHandleValidationErrorsWithNullMessages() {
            // Arrange
            ObjectError error = new FieldError("objectName", "field1", null);
            List<ObjectError> errors = Arrays.asList(error);

            when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(errors);
            when(webRequest.getDescription(false)).thenReturn("uri=/test/path");

            HttpHeaders headers = new HttpHeaders();

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                    methodArgumentNotValidException, headers, HttpStatus.BAD_REQUEST, webRequest);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Validation failed", body.get("message"));
        }

        @Test
        @DisplayName("Should handle single validation error")
        @SuppressWarnings("unchecked")
        void shouldHandleSingleValidationError() {
            // Arrange
            ObjectError error = new FieldError("objectName", "field1", "Field is invalid");
            List<ObjectError> errors = Arrays.asList(error);

            when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getAllErrors()).thenReturn(errors);
            when(webRequest.getDescription(false)).thenReturn("uri=/test/path");

            HttpHeaders headers = new HttpHeaders();

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                    methodArgumentNotValidException, headers, HttpStatus.BAD_REQUEST, webRequest);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Field is invalid", body.get("message"));
        }
    }

    @Nested
    @DisplayName("MethodArgumentTypeMismatchException Tests")
    class MethodArgumentTypeMismatchExceptionTests {

        @Test
        @DisplayName("Should handle type mismatch exception")
        void shouldHandleTypeMismatchException() throws IOException {
            // Act
            exceptionHandler.handleTypeMismatch(httpServletResponse);

            // Assert
            verify(httpServletResponse).sendError(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    @DisplayName("NotFoundException Tests")
    class NotFoundExceptionTests {

        @Test
        @DisplayName("Should handle NotFoundException with message")
        @SuppressWarnings("unchecked")
        void shouldHandleNotFoundExceptionWithMessage() {
            // Arrange
            NotFoundException exception = new NotFoundException("Resource not found");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleNotFoundException(exception);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.NOT_FOUND, body.get("status"));
            assertEquals("Resource not found", body.get("message"));
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Should handle NotFoundException with null message")
        @SuppressWarnings("unchecked")
        void shouldHandleNotFoundExceptionWithNullMessage() {
            // Arrange
            NotFoundException exception = new NotFoundException(null);

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleNotFoundException(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Resource not found", body.get("message"));
        }
    }

    @Nested
    @DisplayName("IllegalStateTransitionException Tests")
    class IllegalStateTransitionExceptionTests {

        @Test
        @DisplayName("Should handle IllegalStateTransitionException with message")
        @SuppressWarnings("unchecked")
        void shouldHandleIllegalStateTransitionExceptionWithMessage() {
            // Arrange
            IllegalStateTransitionException exception = new IllegalStateTransitionException("Invalid state transition");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIllegalStateTransitionException(exception);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.BAD_REQUEST, body.get("status"));
            assertEquals("Invalid state transition", body.get("message"));
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Should handle IllegalStateTransitionException with null message")
        @SuppressWarnings("unchecked")
        void shouldHandleIllegalStateTransitionExceptionWithNullMessage() {
            // Arrange
            IllegalStateTransitionException exception = new IllegalStateTransitionException(null);

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIllegalStateTransitionException(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Invalid state transition", body.get("message"));
        }
    }

    @Nested
    @DisplayName("IllegalOperationException Tests")
    class IllegalOperationExceptionTests {

        @Test
        @DisplayName("Should handle IllegalOperationException with message")
        @SuppressWarnings("unchecked")
        void shouldHandleIllegalOperationExceptionWithMessage() {
            // Arrange
            IllegalOperationException exception = new IllegalOperationException("Operation not allowed");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIllegalOperationException(exception);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.BAD_REQUEST, body.get("status"));
            assertEquals("Operation not allowed", body.get("message"));
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Should handle IllegalOperationException with null message")
        @SuppressWarnings("unchecked")
        void shouldHandleIllegalOperationExceptionWithNullMessage() {
            // Arrange
            IllegalOperationException exception = new IllegalOperationException(null);

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIllegalOperationException(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Illegal operation", body.get("message"));
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException Tests")
    class DataIntegrityViolationExceptionTests {

        @Test
        @DisplayName("Should handle DataIntegrityViolationException with root cause message")
        @SuppressWarnings("unchecked")
        void shouldHandleDataIntegrityViolationExceptionWithRootCause() {
            // Arrange
            Throwable rootCause = new RuntimeException("Duplicate key violation");
            DataIntegrityViolationException exception = new DataIntegrityViolationException("DB error", rootCause);

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolationException(exception);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.CONFLICT, body.get("status"));
            assertEquals("Duplicate key violation", body.get("message"));
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException with root cause but empty message")
        @SuppressWarnings("unchecked")
        void shouldHandleDataIntegrityViolationExceptionWithEmptyRootCauseMessage() {
            // Arrange
            Throwable rootCause = new RuntimeException("   ");
            DataIntegrityViolationException exception = new DataIntegrityViolationException("Constraint violation",
                    rootCause);

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolationException(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            // Since root cause exists but has empty message, it uses the default message
            // (doesn't fall back to exception message due to else-if structure)
            assertEquals("Database constraint violation", body.get("message"));
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException without root cause")
        @SuppressWarnings("unchecked")
        void shouldHandleDataIntegrityViolationExceptionWithoutRootCause() {
            // Arrange
            DataIntegrityViolationException exception = new DataIntegrityViolationException("Constraint error");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolationException(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Constraint error", body.get("message"));
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException with null root cause and empty message")
        @SuppressWarnings("unchecked")
        void shouldHandleDataIntegrityViolationExceptionWithNullRootCauseAndEmptyMessage() {
            // Arrange
            DataIntegrityViolationException exception = new DataIntegrityViolationException("   ");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolationException(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Database constraint violation", body.get("message"));
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException with null message")
        @SuppressWarnings("unchecked")
        void shouldHandleDataIntegrityViolationExceptionWithNullMessage() {
            // Arrange
            DataIntegrityViolationException exception = new DataIntegrityViolationException(null);

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolationException(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Database constraint violation", body.get("message"));
        }
    }

    @Nested
    @DisplayName("IOException Tests")
    class IOExceptionTests {

        @Test
        @DisplayName("Should handle IOException with message")
        @SuppressWarnings("unchecked")
        void shouldHandleIOExceptionWithMessage() {
            // Arrange
            IOException exception = new IOException("File not found");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIOException(exception);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, body.get("status"));
            assertEquals("File not found", body.get("message"));
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Should handle IOException with null message")
        @SuppressWarnings("unchecked")
        void shouldHandleIOExceptionWithNullMessage() {
            // Arrange
            IOException exception = new IOException();

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIOException(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("I/O error occurred", body.get("message"));
        }
    }

    @Nested
    @DisplayName("RuntimeException Tests")
    class RuntimeExceptionTests {

        @Test
        @DisplayName("Should handle RuntimeException with message")
        @SuppressWarnings("unchecked")
        void shouldHandleRuntimeExceptionWithMessage() {
            // Arrange
            RuntimeException exception = new RuntimeException("Unexpected error");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleOtherRuntimeException(exception);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, body.get("status"));
            assertEquals("Unexpected error", body.get("message"));
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Should handle RuntimeException with null message")
        @SuppressWarnings("unchecked")
        void shouldHandleRuntimeExceptionWithNullMessage() {
            // Arrange
            RuntimeException exception = new RuntimeException();

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleOtherRuntimeException(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals("Internal server error", body.get("message"));
        }
    }

    @Nested
    @DisplayName("Generic Exception Tests")
    class GenericExceptionTests {

        @Test
        @DisplayName("Should handle generic Exception with generic message")
        @SuppressWarnings("unchecked")
        void shouldHandleGenericException() {
            // Arrange
            Exception exception = new Exception("Some unexpected error");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleExceptions(exception);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, body.get("status"));
            assertEquals("Unexpected Server Error", body.get("message"));
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Should always return generic message for Exception")
        @SuppressWarnings("unchecked")
        void shouldAlwaysReturnGenericMessageForException() {
            // Arrange
            Exception exception = new Exception("Specific error details that should not be exposed");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleExceptions(exception);

            // Assert
            assertNotNull(response);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            // Should return generic message, not the specific exception message
            assertEquals("Unexpected Server Error", body.get("message"));
        }
    }

    @Nested
    @DisplayName("Response Structure Tests")
    class ResponseStructureTests {

        @Test
        @DisplayName("Should always include required fields in response body")
        @SuppressWarnings("unchecked")
        void shouldAlwaysIncludeRequiredFieldsInResponseBody() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Test error");
            when(webRequest.getDescription(false)).thenReturn("uri=/test/path");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(exception, webRequest);

            // Assert
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertTrue(body.containsKey("timestamp"));
            assertTrue(body.containsKey("status"));
            assertTrue(body.containsKey("message"));
            assertTrue(body.containsKey("path"));
            assertEquals(4, body.size());
        }

        @Test
        @DisplayName("Should return empty HttpHeaders")
        void shouldReturnEmptyHttpHeaders() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Test error");
            when(webRequest.getDescription(false)).thenReturn("uri=/test/path");

            // Act
            ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(exception, webRequest);

            // Assert
            HttpHeaders headers = response.getHeaders();
            assertNotNull(headers);
            assertTrue(headers.isEmpty());
        }
    }
}
