package com.project.core_service.exceptions;

/**
 * Exception thrown to indicate that an entity with the given ID was not found.
 *
 * <p>This is a runtime exception typically used when a requested resource
 * does not exist in the system (e.g., database lookup failure, missing record, etc.).</p>
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code NotFoundException} with a detail message
     * indicating that the specified ID could not be found.
     *
     * @param id the identifier of the entity that was not found
     */
    public NotFoundException(String id) {
        super(String.format("id %s not found", id));
    }
}
