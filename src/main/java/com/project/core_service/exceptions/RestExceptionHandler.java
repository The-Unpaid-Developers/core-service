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
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Global exception handler for REST controllers.
 *
 * <p>This class extends {@link ResponseEntityExceptionHandler} and uses
 * {@link ControllerAdvice} to handle and customize responses for various
 * exceptions thrown within the application.</p>
 *
 * <p>It ensures that clients receive consistent, JSON-formatted error
 * responses containing a timestamp, HTTP status, message, and request path.</p>
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Handles validation errors for invalid method arguments.
     *
     * @param ex the exception containing validation errors
     * @param headers HTTP headers
     * @param status HTTP status code
     * @param request the current web request
     * @return a {@link ResponseEntity} containing error details
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status.value());
        StringBuilder message = new StringBuilder();
        for (ObjectError objectError : ex.getBindingResult().getAllErrors()) {
            message.append(objectError.getDefaultMessage());
        }
        body.put("message", message.toString());
        body.put("path", request.getDescription(false));
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
    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status);
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    /**
     * Handles uncaught {@link RuntimeException}s.
     *
     * @param ex the runtime exception
     * @return a {@link ResponseEntity} with error details
     */
    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Object> handleOtherRuntimeException(RuntimeException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status);
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    /**
     * Handles database constraint violations such as unique key violations.
     *
     * @param ex the {@link DataIntegrityViolationException}
     * @return a {@link ResponseEntity} with conflict details
     */
    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status);
        if (ex.getRootCause() != null) {
            body.put("message", ex.getRootCause().getMessage());
        } else {
            body.put("message", ex.getMessage());
        }
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    /**
     * Handles {@link IOException}s thrown during request processing.
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with error details
     */
    @ExceptionHandler({IOException.class})
    public ResponseEntity<Object> handleIOException(IOException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status);
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    /**
     * Handles file upload errors where the file exceeds the maximum allowed size.
     *
     * @param ex the {@link MaxUploadSizeExceededException}
     * @return a {@link ResponseEntity} with error details
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        HttpStatus status = HttpStatus.EXPECTATION_FAILED;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status);
        body.put("message", "File too large!");
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }
}
