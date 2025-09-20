package com.project.core_service.models.solutions_review;

import java.util.Set;
import java.util.EnumSet;
import java.util.Map;
import java.util.List;
import com.project.core_service.exceptions.IllegalStateTransitionException;

/**
 * Represents the lifecycle states of a solution review document.
 * <p>
 * This enum tracks the document through its review and approval process,
 * from initial creation to final approval and eventual obsolescence.
 * State transitions typically follow: DRAFT → SUBMITTED → APPROVED → ACTIVE →
 * OUTDATED
 * 
 * @see com.project.core_service.models.solutions_review.SolutionReview
 * @since 1.0
 */
public enum DocumentState {

    /**
     * Initial state when a solution review is first created.
     * <p>
     * In this state, the document can be freely edited and is not yet
     * ready for review. Documents in DRAFT state are typically only
     * visible to their creators and can be modified without restrictions.
     * 
     * @see SolutionReview#submit()
     */
    DRAFT,

    /**
     * Document has been submitted for review but not yet approved.
     * <p>
     * Once submitted, the document enters the review process and should
     * not be modified until the review is complete. This state indicates
     * the document is awaiting approval from stakeholders or reviewers.
     * 
     * @see SolutionReview#approve()
     * @see SolutionReview#removeSubmission()
     */
    SUBMITTED,

    /**
     * Document has been approved but not yet activated.
     * <p>
     * This state indicates that the document has passed the review process
     * and is ready to become the active version. From this state, the document
     * can be activated to become the current working version.
     * 
     * @see SolutionReview#activate()
     */
    APPROVED,

    /**
     * Document has been approved and is the current active version.
     * <p>
     * This represents the approved, official version of the solution review
     * that should be used for decision-making and reference. Only one
     * document per solution should typically be in ACTIVE state at a time.
     * 
     * @see SolutionReview#markAsOutdated()
     */
    ACTIVE,

    /**
     * Document was previously active but has been superseded.
     * <p>
     * When a new version of a solution review is approved and activated, the
     * previous
     * ACTIVE document transitions to OUTDATED. This state preserves
     * historical versions while clearly indicating they should not be
     * used for current decision-making.
     * 
     */
    OUTDATED;

    // Define valid transitions for each state
    private static final Set<DocumentState> DRAFT_TRANSITIONS = EnumSet.of(SUBMITTED);
    private static final Set<DocumentState> SUBMITTED_TRANSITIONS = EnumSet.of(APPROVED, DRAFT);
    private static final Set<DocumentState> APPROVED_TRANSITIONS = EnumSet.of(ACTIVE);
    private static final Set<DocumentState> ACTIVE_TRANSITIONS = EnumSet.of(OUTDATED);
    private static final Set<DocumentState> OUTDATED_TRANSITIONS = EnumSet.noneOf(DocumentState.class);

    /**
     * Validates if a transition from this state to the target state is allowed.
     * 
     * @param targetState the state to transition to
     * @return true if the transition is valid, false otherwise
     * @throws IllegalArgumentException if targetState is null
     */
    public boolean canTransitionTo(DocumentState targetState) {
        if (targetState == null) {
            throw new IllegalArgumentException("Target state cannot be null");
        }

        if (this == targetState) {
            return false; // No transition needed for same state
        }

        return getValidTransitions().contains(targetState);
    }

    /**
     * Gets the set of valid states that this state can transition to.
     * 
     * @return an immutable set of valid target states
     */
    public Set<DocumentState> getValidTransitions() {
        switch (this) {
            case DRAFT:
                return DRAFT_TRANSITIONS;
            case SUBMITTED:
                return SUBMITTED_TRANSITIONS;
            case APPROVED:
                return APPROVED_TRANSITIONS;
            case ACTIVE:
                return ACTIVE_TRANSITIONS;
            case OUTDATED:
                return OUTDATED_TRANSITIONS;
            default:
                return EnumSet.noneOf(DocumentState.class);
        }
    }

    /**
     * Validates a state transition and throws an exception if invalid.
     * 
     * @param targetState the state to transition to
     * @throws IllegalStateTransitionException if the transition is not allowed
     * @throws IllegalArgumentException        if targetState is null
     */
    public void validateTransition(DocumentState targetState) {
        if (targetState == null) {
            throw new IllegalArgumentException("Target state cannot be null");
        }

        if (this == targetState) {
            throw new IllegalStateTransitionException(
                    String.format("Document is already in %s state", this));
        }

        if (!canTransitionTo(targetState)) {
            throw new IllegalStateTransitionException(
                    String.format(
                            "Invalid state transition from %s to %s. Valid transitions from %s are: %s",
                            this, targetState, this, getValidTransitions()));
        }
    }

    /**
     * Gets a human-readable description of this state.
     * 
     * @return a description of what this state represents
     */
    public String getDescription() {
        switch (this) {
            case DRAFT:
                return "Document is being edited and is not ready for review";
            case SUBMITTED:
                return "Document has been submitted for review and approval";
            case APPROVED:
                return "Document has been approved and is ready to be activated";
            case ACTIVE:
                return "Document is approved and represents the current active version";
            case OUTDATED:
                return "Document was previously active but has been superseded";
            default:
                return "Unknown state";
        }
    }

    /**
     * Checks if this state allows document editing.
     * 
     * @return true if the document can be edited in this state
     */
    public boolean isEditable() {
        return this == DRAFT;
    }

    /**
     * Checks if this state represents a finalized document.
     * 
     * @return true if the document is in a final state (ACTIVE or OUTDATED)
     */
    public boolean isFinalized() {
        return this == ACTIVE || this == OUTDATED;
    }

    /**
     * Checks if this state requires exclusive existence constraint.
     * <p>
     * Only one document per system should exist in DRAFT, SUBMITTED, APPROVED, or
     * ACTIVE
     * states at any given time. This ensures proper workflow management and
     * prevents
     * conflicting working versions.
     * 
     * @return true if only one document should exist in this state per system
     */
    public boolean requiresExclusiveConstraint() {
        return this == DRAFT || this == SUBMITTED || this == APPROVED || this == ACTIVE;
    }

    /**
     * Gets all states that should have exclusive constraint (max 1 document per
     * system).
     * 
     * @return set of states that require exclusive existence
     */
    public static Set<DocumentState> getExclusiveStates() {
        return EnumSet.of(DRAFT, SUBMITTED, APPROVED, ACTIVE);
    }

    /**
     * Gets all states that allow multiple documents per system.
     * 
     * @return set of states that allow multiple documents
     */
    public static Set<DocumentState> getNonExclusiveStates() {
        return EnumSet.of(OUTDATED);
    }

    /**
     * Enum defining all possible state transition operations
     */
    public enum StateOperation {
        SUBMIT("submit document", SUBMITTED),
        REMOVE_SUBMISSION("remove submission", DRAFT),
        APPROVE("approve document", APPROVED),
        ACTIVATE("activate document", ACTIVE),
        UNAPPROVE("un-approve document", SUBMITTED),
        MARK_OUTDATED("mark as outdated", OUTDATED);

        private final String operationName;
        private final DocumentState targetState;

        StateOperation(String operationName, DocumentState targetState) {
            this.operationName = operationName;
            this.targetState = targetState;
        }

        public String getOperationName() {
            return operationName;
        }

        public DocumentState getTargetState() {
            return targetState;
        }
    }

    /**
     * Map defining which operations are allowed from which states
     */
    private static final Map<StateOperation, DocumentState> OPERATION_REQUIREMENTS = Map.of(
            StateOperation.SUBMIT, DRAFT,
            StateOperation.REMOVE_SUBMISSION, SUBMITTED,
            StateOperation.APPROVE, SUBMITTED,
            StateOperation.ACTIVATE, APPROVED,
            StateOperation.UNAPPROVE, APPROVED,
            StateOperation.MARK_OUTDATED, ACTIVE);

    /**
     * Validates if an operation can be executed from this state.
     * 
     * @param operation the operation to validate
     * @return true if the operation is allowed from this state
     */
    public boolean canExecuteOperation(StateOperation operation) {
        DocumentState requiredState = OPERATION_REQUIREMENTS.get(operation);
        return this == requiredState;
    }

    /**
     * Get all operations that are allowed from this state.
     * 
     * @return list of operations that can be executed from this state
     */
    public List<StateOperation> getAvailableOperations() {
        return OPERATION_REQUIREMENTS.entrySet().stream()
                .filter(entry -> entry.getValue() == this)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Validates and executes a state transition operation.
     * 
     * @param operation the operation to execute
     * @return the new state after the operation
     * @throws IllegalStateTransitionException if the operation is not allowed from
     *                                         this state
     */
    public DocumentState executeOperation(StateOperation operation) {
        DocumentState requiredState = OPERATION_REQUIREMENTS.get(operation);
        if (this != requiredState) {
            throw new IllegalStateTransitionException(
                    String.format("Cannot %s: document state is %s but must be %s",
                            operation.getOperationName(), this, requiredState));
        }
        return operation.getTargetState();
    }
}
