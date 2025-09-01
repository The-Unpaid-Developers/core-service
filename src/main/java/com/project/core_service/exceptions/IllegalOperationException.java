package com.project.core_service.exceptions;

/**
 * Exception thrown to indicate that an operation is not allowed.
 *
 * <p>
 * This is a runtime exception typically used when a requested action
 * cannot be performed due to business rules or constraints.
 * </p>
 */
public class IllegalOperationException extends RuntimeException {
    /**
     * Constructs a new {@code IllegalOperationException} with a detail message
     * indicating that the operation is not allowed.
     *
     * @param msg message
     */
    public IllegalOperationException(String msg) {
        super(msg);
    }
}
