package com.project.core_service.commands;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command object representing a request to transition a SolutionReview document
 * through its lifecycle states.
 * 
 * This command encapsulates all the information needed to perform a state
 * transition operation on a document, following the Command pattern.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LifecycleTransitionCommand {

    /**
     * The ID of the SolutionReview document to transition
     */
    @NotBlank(message = "Document ID is required")
    private String documentId;

    /**
     * The specific operation to perform (e.g., SUBMIT, APPROVE, etc.)
     */
    @NotBlank(message = "Operation is required")
    private String operation;

    /**
     * The user performing the transition (for audit purposes)
     */
    @NotBlank(message = "Modified by user is required")
    private String modifiedBy;

    /**
     * Optional comment or reason for the transition
     */
    private String comment;

    /**
     * Factory method for creating a command with required fields
     */
    public static LifecycleTransitionCommand of(String documentId,
            String operation,
            String modifiedBy) {
        return new LifecycleTransitionCommand(documentId, operation, modifiedBy, null);
    }

    /**
     * Factory method for creating a command with a comment
     */
    public static LifecycleTransitionCommand withComment(String documentId,
            String operation,
            String modifiedBy,
            String comment) {
        return new LifecycleTransitionCommand(documentId, operation, modifiedBy, comment);
    }
}
