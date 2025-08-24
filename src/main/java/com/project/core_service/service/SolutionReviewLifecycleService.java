package com.project.core_service.service;

import com.project.core_service.commands.LifecycleTransitionCommand;
import com.project.core_service.exceptions.IllegalStateTransitionException;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.audit.AuditLogMeta;
import com.project.core_service.models.audit.AuditLogNode;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.repositories.SolutionReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Service responsible for managing the lifecycle transitions of SolutionReview
 * documents.
 * 
 * This service handles all state transitions (DRAFT → SUBMITTED → CURRENT →
 * OUTDATED)
 * and ensures business rules are enforced during transitions.
 */
@Service
@Slf4j
public class SolutionReviewLifecycleService {

    private final SolutionReviewRepository solutionReviewRepository;
    private final AuditLogService auditLogService;
    private final VersionService versionService;

    @Autowired
    public SolutionReviewLifecycleService(SolutionReviewRepository solutionReviewRepository,
            AuditLogService auditLogService, VersionService versionService) {
        this.solutionReviewRepository = solutionReviewRepository;
        this.auditLogService = auditLogService;
        this.versionService = versionService;
    }

    /**
     * Executes a lifecycle transition for a SolutionReview document.
     * 
     * @param command the transition command containing document ID, operation, and
     *                user info
     * @throws IllegalStateTransitionException if the transition is not allowed
     * @throws NotFoundException               if the document is not found
     */
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
        // we have special handling for APPROVE and UNAPPROVE
        // to ensure that we have only 1 current SR for a systemCode
        switch (operation) {
            case APPROVE:
                executeApprove(solutionReview, command.getModifiedBy());
                break;
            case UNAPPROVE:
                executeUnapprove(solutionReview, command.getModifiedBy());
                break;
            default:
                executeTransitionOperationOnSolutionReview(solutionReview, operation, command.getModifiedBy());
                break;
        }

        log.debug("State transition successful: {} -> {}",
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
     * 
     * @param solutionReview the document to operate on
     * @param operation      the operation to execute
     * @param modifiedBy     the user performing the operation
     */
    private void executeTransitionOperationOnSolutionReview(SolutionReview solutionReview,
            DocumentState.StateOperation operation,
            String modifiedBy) {

        switch (operation) {
            case SUBMIT:
                solutionReview.submit(modifiedBy);
                break;
            case REMOVE_SUBMISSION:
                solutionReview.removeSubmission(modifiedBy);
                break;
            case MARK_OUTDATED:
                solutionReview.markAsOutdated(modifiedBy);
                break;
            case RESET_CURRENT:
                solutionReview.resetAsCurrent(modifiedBy);
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
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
            case REMOVE_SUBMISSION -> DocumentState.SUBMITTED;
            case APPROVE -> DocumentState.SUBMITTED;
            case UNAPPROVE -> DocumentState.CURRENT;
            case MARK_OUTDATED -> DocumentState.CURRENT;
            case RESET_CURRENT -> DocumentState.OUTDATED;
        };
    }

    /**
     * Executes APPROVE operation with special handling:
     * 1. Find current SR for the same systemCode using audit log and mark it as
     * outdated
     * 2. Approve the submitted SR (making it current)
     * 3. Update audit log to track the new current SR
     * 
     * @param solutionReview the SR to approve (must be in SUBMITTED state)
     * @param modifiedBy     the user performing the operation
     */
    private void executeApprove(SolutionReview solutionReview, String modifiedBy) {
        log.info("Executing APPROVE for systemCode: {}", solutionReview.getSystemCode());

        // step 1: retrieve the audit log meta for this system code
        AuditLogMeta auditLogMeta = auditLogService.getAuditLogMeta(solutionReview.getSystemCode());

        if (auditLogMeta == null) {
            auditLogMeta = new AuditLogMeta(solutionReview.getId(),
                    solutionReview.getSystemCode());

            auditLogService.createAuditLogMeta(auditLogMeta);
        }

        // step 2: find and mark existing current SR as outdated
        String currentSRVersion = findAndMarkExistingCurrentSRAsOutdated(solutionReview, modifiedBy, auditLogMeta);

        // Step 3: Approve the submitted SR (making it CURRENT)
        solutionReview.approve(modifiedBy);

        // step 4: increment the version number
        String newSRVersion = versionService.incrementPatchVersion(currentSRVersion);

        // step 5: Add the new current SR to audit log
        auditLogService.addSolutionReviewToAuditLog(auditLogMeta, solutionReview, "Approved and set as current",
                newSRVersion);

        log.info("APPROVE completed: SR {} is now current, existing current SR: {}",
                solutionReview.getId(), auditLogMeta.getHead() != null ? auditLogMeta.getHead() : "none");
    }

    private String findAndMarkExistingCurrentSRAsOutdated(SolutionReview solutionReview, String modifiedBy,
            AuditLogMeta auditLogMeta) {

        AuditLogNode headNode = auditLogService.getAuditLogNode(auditLogMeta.getHead());
        if (headNode == null) {
            log.info("Head node not found for audit log meta: {}", auditLogMeta.getId());
            return null;
        }

        String srVersion = headNode.getSolutionsReviewVersion();

        // find the current SR and mark it as outdated
        Optional<SolutionReview> existingCurrentSROpt = solutionReviewRepository.findById(srVersion);
        if (existingCurrentSROpt.isPresent()
                && existingCurrentSROpt.get().getDocumentState() == DocumentState.CURRENT) {
            existingCurrentSROpt.get().markAsOutdated(modifiedBy);

            // update the SR
            solutionReviewRepository.save(existingCurrentSROpt.get());

            // update the audit log
            auditLogService.updateAuditLogNode(headNode, "Marked as outdated due to new approval");
        }
        return srVersion;
    }

    private void executeUnapprove(SolutionReview solutionReview, String modifiedBy) {
        log.info("Executing UNAPPROVE for systemCode: {}", solutionReview.getSystemCode());

        // step 1: retrieve the audit log meta for this system code
        AuditLogMeta auditLogMeta = auditLogService.getAuditLogMeta(solutionReview.getSystemCode());

        if (auditLogMeta == null) {
            throw new NotFoundException(String.format("Audit log meta not found for systemCode: %s",
                    solutionReview.getSystemCode()));
        }

        // step 2: unapprove the current SR
        solutionReview.unApproveCurrent(modifiedBy);

        // step 3: remove this SR from the audit log
        String headNodeId = auditLogMeta.getHead();
        if (headNodeId == null) {
            throw new IllegalStateException("Cannot unapprove: audit log has no head node for systemCode: "
                    + solutionReview.getSystemCode());
        }

        AuditLogNode headNode = auditLogService.getAuditLogNode(headNodeId);
        if (headNode == null) {
            throw new NotFoundException("Head node not found in audit log for systemCode: "
                    + solutionReview.getSystemCode());
        }

        AuditLogNode nextNode = headNode.getNext();
        if (nextNode == null) {
            throw new IllegalStateException("Cannot unapprove: no previous version available for systemCode: "
                    + solutionReview.getSystemCode());
        }

        auditLogMeta.removeHead(nextNode.getId());

        // step 4: update the audit log meta
        auditLogService.updateAuditLogMeta(auditLogMeta);

        // step 5: update the most recent outdated as current
        Optional<SolutionReview> mostRecentOutdated = solutionReviewRepository.findById(nextNode.getSolutionReviewId());
        if (mostRecentOutdated.isPresent()) {
            mostRecentOutdated.get().resetAsCurrent(modifiedBy);
            solutionReviewRepository.save(mostRecentOutdated.get());
        }

        log.info("UNAPPROVE completed: SR {} is now outdated, existing current SR: {}",
                solutionReview.getId(), auditLogMeta.getHead() != null ? auditLogMeta.getHead() : "none");
    }

}
