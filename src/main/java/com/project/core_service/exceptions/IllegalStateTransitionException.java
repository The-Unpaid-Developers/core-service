package com.project.core_service.exceptions;

/**
 * Custom exception for invalid state transitions.
 * <p>
 * This exception is thrown when an attempt is made to transition between states
 * that are not allowed according to the defined state machine rules.
 * 
 * @since 1.0
 */
public class IllegalStateTransitionException extends RuntimeException {

    /**
     * Constructs a new IllegalStateTransitionException with the specified detail
     * message.
     * 
     * @param message the detail message explaining the invalid transition
     */
    public IllegalStateTransitionException(String message) {
        super(message);
    }

    /**
     * Constructs a new IllegalStateTransitionException with the specified detail
     * message and cause.
     * 
     * @param message the detail message explaining the invalid transition
     * @param cause   the cause of this exception
     */
    public IllegalStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
