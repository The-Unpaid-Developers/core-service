package com.project.core_service.exceptions;

/**
 * Exception thrown when an uploaded file is invalid or has incorrect format.
 *
 * <p>
 * This exception is typically used when validating file uploads to indicate
 * issues such as:
 * <ul>
 * <li>Incorrect file type or extension</li>
 * <li>Empty or missing file</li>
 * <li>Corrupted file content</li>
 * <li>Invalid file structure</li>
 * </ul>
 * </p>
 */
public class InvalidFileException extends RuntimeException {

    /**
     * Constructs a new {@code InvalidFileException} with a detail message.
     *
     * @param message the detail message explaining why the file is invalid
     */
    public InvalidFileException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code InvalidFileException} with a detail message and cause.
     *
     * @param message the detail message explaining why the file is invalid
     * @param cause   the underlying cause of the validation failure
     */
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
