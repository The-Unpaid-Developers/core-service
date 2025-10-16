package com.project.core_service.exceptions;

/**
 * Exception thrown when CSV file processing fails.
 *
 * <p>
 * This exception is used to indicate errors during CSV parsing, validation,
 * or transformation operations.
 * </p>
 */
public class CsvProcessingException extends RuntimeException {

    /**
     * Constructs a new {@code CsvProcessingException} with a detail message.
     *
     * @param message the detail message explaining the processing failure
     */
    public CsvProcessingException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code CsvProcessingException} with a detail message and cause.
     *
     * @param message the detail message explaining the processing failure
     * @param cause   the underlying cause of the processing failure
     */
    public CsvProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
