package com.project.core_service.services;

import com.project.core_service.commands.LifecycleTransitionCommand;
import com.project.core_service.exceptions.IllegalStateTransitionException;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.repositories.SolutionReviewRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing the lifecycle transitions of SolutionReview
 * documents.
 * 
 * This service handles all state transitions (DRAFT → SUBMITTED → APPROVED →
 * ACTIVE → OUTDATED)
 * and ensures business rules are enforced during transitions.
 */
@Service
@Slf4j
public class SolutionReviewLifecycleService {

    private final SolutionReviewRepository solutionReviewRepository;
    private final SolutionReviewService solutionReviewService;

    @Autowired
    public SolutionReviewLifecycleService(SolutionReviewRepository solutionReviewRepository,
            SolutionReviewService solutionReviewService) {
        this.solutionReviewRepository = solutionReviewRepository;
        this.solutionReviewService = solutionReviewService;
    }

    /**
     * Executes a lifecycle transition for a SolutionReview document.
     * Uses transaction management to ensure atomicity - if any part fails,
     * all changes are rolled back to maintain data consistency.
     * 
     * @param command the transition command containing document ID, operation, and
     *                user info
     * @throws IllegalStateTransitionException if the transition is not allowed
     * @throws NotFoundException               if the document is not found
     */
    @Transactional
    public void executeTransition(LifecycleTransitionCommand command) {
        log.info("Executing lifecycle transition: operation={}, documentId={}, user={}",
                command.getOperation(), command.getDocumentId(), command.getModifiedBy());

        // step 0: Validate and convert operation string to enum
        DocumentState.StateOperation operation;
        try {
            operation = DocumentState.StateOperation.valueOf(command.getOperation().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid operation '%s'. Valid operations are: %s",
                            command.getOperation(),
                            java.util.Arrays.toString(DocumentState.StateOperation.values())));
        }

        // step 1: Load SolutionReview by documentId
        SolutionReview solutionReview = solutionReviewRepository.findById(command.getDocumentId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("SolutionReview with ID '%s' not found", command.getDocumentId())));

        DocumentState currentState = solutionReview.getDocumentState();

        log.info("Found SolutionReview: id={}, currentState={}, requestedOperation={}",
                solutionReview.getId(), currentState, operation);

        // step 2: Validate the transition is allowed using the built-in state
        if (!currentState.canExecuteOperation(operation)) {
            String errorMessage = String.format(
                    "Cannot execute operation '%s' on document '%s'. Document is in state '%s' but operation requires state '%s'. Available operations: %s",
                    operation.getOperationName(),
                    command.getDocumentId(),
                    currentState,
                    getRequiredStateForOperation(operation),
                    currentState.getAvailableOperations());

            log.error("Invalid state transition attempted: {}", errorMessage);
            throw new IllegalStateTransitionException(errorMessage);
        }

        // step 3: Execute the state operation
        executeTransitionOperationOnSolutionReview(solutionReview, operation, command.getModifiedBy());

        log.info("State transition successful: {} -> {}",
                currentState, solutionReview.getDocumentState());

        // step 4: Save the updated document
        SolutionReview savedReview = solutionReviewRepository.save(solutionReview);

        log.info("Lifecycle transition completed successfully: documentId={}, operation={}, " +
                "oldState={}, newState={}, modifiedBy={}, comment='{}'",
                savedReview.getId(),
                operation.getOperationName(),
                currentState,
                savedReview.getDocumentState(),
                command.getModifiedBy(),
                command.getComment());
    }

    /**
     * Executes the specific transition operation on the SolutionReview
     * with constraint validation for exclusive states.
     * 
     * @param solutionReview the document to operate on
     * @param operation      the operation to execute
     * @param modifiedBy     the user performing the operation
     */
    private void executeTransitionOperationOnSolutionReview(SolutionReview solutionReview,
            DocumentState.StateOperation operation,
            String modifiedBy) {

        DocumentState targetState = operation.getTargetState();

        // For ACTIVATE operation, deactivate existing active SRs first
        if (operation == DocumentState.StateOperation.ACTIVATE) {
            deactivateExistingActiveSR(solutionReview, modifiedBy);
        }

        // Validate constraints for transitions to states that require exclusivity
        if (targetState.requiresExclusiveConstraint()) {
            // Use excludeId to allow the current document to remain during the transition
            solutionReviewService.validateExclusiveStateConstraint(
                    solutionReview.getSystemCode(),
                    solutionReview.getId());
        } else if (targetState == DocumentState.ACTIVE) {
            // Validate ACTIVE state constraint separately (singular exclusive constraint)
            solutionReviewService.validateActiveStateConstraint(
                    solutionReview.getSystemCode(),
                    solutionReview.getId());
        }

        switch (operation) {
            case SUBMIT:
                solutionReview.submit(modifiedBy);
                break;
            case REMOVE_SUBMISSION:
                solutionReview.removeSubmission(modifiedBy);
                break;
            case APPROVE:
                solutionReview.approve(modifiedBy);
                break;
            case ACTIVATE:
                solutionReview.activate(modifiedBy);
                break;
            case UNAPPROVE:
                solutionReview.unApprove(modifiedBy);
                break;
            case MARK_OUTDATED:
                solutionReview.markAsOutdated(modifiedBy);
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    /**
     * Helper method to deactivate any existing active Solution Review with the same systemCode
     * before activating a new one. This ensures only one SR per systemCode can be active at a time.
     * 
     * @param currentSolutionReview the solution review that is about to be activated
     * @param modifiedBy the user performing the operation
     */
    private void deactivateExistingActiveSR(SolutionReview currentSolutionReview, String modifiedBy) {
        String systemCode = currentSolutionReview.getSystemCode();
        
        // Find any existing active SR with the same systemCode
        SolutionReview existingActiveSR = solutionReviewRepository
            .findBySystemCodeAndDocumentState(systemCode, DocumentState.ACTIVE)
            .orElse(null);
        
        if (existingActiveSR != null && !existingActiveSR.getId().equals(currentSolutionReview.getId())) {
            log.info("Found existing active Solution Review with ID: {} for systemCode: {}. Marking as outdated.", 
                existingActiveSR.getId(), systemCode);
            
            // Transition the existing active SR to outdated
            existingActiveSR.markAsOutdated(modifiedBy);
            solutionReviewRepository.save(existingActiveSR);
            
            log.info("Successfully marked Solution Review {} as outdated", existingActiveSR.getId());
        }
    }

    /**
     * Helper method to get the required state for an operation (for error
     * messages).
     * 
     * @param operation the operation
     * @return the required state
     */
    private DocumentState getRequiredStateForOperation(DocumentState.StateOperation operation) {
        return switch (operation) {
            case SUBMIT -> DocumentState.DRAFT;
            case REMOVE_SUBMISSION, APPROVE -> DocumentState.SUBMITTED;
            case ACTIVATE, UNAPPROVE -> DocumentState.APPROVED;
            case MARK_OUTDATED -> DocumentState.ACTIVE;
        };
    }

}
