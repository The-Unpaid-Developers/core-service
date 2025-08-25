package com.project.core_service.exceptions;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Global exception handler for REST controllers.
 *
 * <p>
 * This class extends {@link ResponseEntityExceptionHandler} and uses
 * {@link ControllerAdvice} to handle and customize responses for various
 * exceptions thrown within the application.
 * </p>
 *
 * <p>
 * It ensures that clients receive consistent, JSON-formatted error
 * responses containing a timestamp, HTTP status, message, and request path.
 * </p>
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE = "message";
    private static final String PATH = "path";
    private static final String STATUS = "status";

    /**
     * Handles validation errors for invalid method arguments.
     *
     * @param ex      the exception containing validation errors
     * @param headers HTTP headers
     * @param status  HTTP status code
     * @param request the current web request
     * @return a {@link ResponseEntity} containing error details
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, new Date());
        body.put(STATUS, status.value());
        StringBuilder message = new StringBuilder();
        for (ObjectError objectError : ex.getBindingResult().getAllErrors()) {
            String errorMessage = objectError.getDefaultMessage();
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                if (!message.isEmpty()) {
                    message.append("; ");
                }
                message.append(errorMessage);
            }
        }
        // Fallback message if no valid error messages were found
        String finalMessage = !message.isEmpty() ? message.toString() : "Validation failed";
        body.put(MESSAGE, finalMessage);
        body.put(PATH, request.getDescription(false));
        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Handles cases where a request parameter has the wrong type.
     *
     * @param response the HTTP response to send the error
     * @throws IOException if an I/O error occurs while sending the response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public void handleTypeMismatch(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Handles uncaught {@link NotFoundException}s.
     *
     * @param ex the runtime exception
     * @return a {@link ResponseEntity} with error details
     */
    @ExceptionHandler({ NotFoundException.class })
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, new Date());
        body.put(STATUS, status);
        String message = ex.getMessage();
        body.put(MESSAGE, message != null ? message : "Resource not found");
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    /**
     * Handles uncaught {@link IllegalStateTransitionException}s.
     *
     * @param ex the illegal state transition exception
     * @return a {@link ResponseEntity} with error details
     */
    @ExceptionHandler({ IllegalStateTransitionException.class })
    public ResponseEntity<Object> handleIllegalStateTransitionException(IllegalStateTransitionException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, new Date());
        body.put(STATUS, status);
        String message = ex.getMessage();
        body.put(MESSAGE, message != null ? message : "Invalid state transition");
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    /**
     * Handles database constraint violations such as unique key violations.
     *
     * @param ex the {@link DataIntegrityViolationException}
     * @return a {@link ResponseEntity} with conflict details
     */
    @ExceptionHandler({ DataIntegrityViolationException.class })
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, new Date());
        body.put(STATUS, status);

        String message = "Database constraint violation";
        Throwable rootCause = ex.getRootCause();
        if (rootCause != null) {
            String rootCauseMessage = rootCause.getMessage();
            if (rootCauseMessage != null && !rootCauseMessage.trim().isEmpty()) {
                message = rootCauseMessage;
            }
        } else if (ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
            message = ex.getMessage();
        }

        body.put(MESSAGE, message);
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    /**
     * Handles {@link IOException}s thrown during request processing.
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with error details
     */
    @ExceptionHandler({ IOException.class })
    public ResponseEntity<Object> handleIOException(IOException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, new Date());
        body.put(STATUS, status);
        String message = ex.getMessage();
        body.put(MESSAGE, message != null ? message : "I/O error occurred");
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    /**
     * Handles uncaught {@link RuntimeException}s that don't have specific handlers.
     *
     * @param ex the runtime exception
     * @return a {@link ResponseEntity} with error details
     */
    @ExceptionHandler({ RuntimeException.class })
    public ResponseEntity<Object> handleOtherRuntimeException(RuntimeException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, new Date());
        body.put(STATUS, status);
        String message = ex.getMessage();
        body.put(MESSAGE, message != null ? message : "Internal server error");
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    /**
     * Universal fallback handler for all uncaught exceptions.
     * This prevents raw stack traces from being exposed to the client.
     * Always returns a generic error response.
     */
    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleExceptions(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, new Date());
        body.put(STATUS, status);
        body.put(MESSAGE, "Unexpected Server Error");
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }
}
