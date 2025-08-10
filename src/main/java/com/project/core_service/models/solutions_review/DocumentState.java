package com.project.core_service.models.solutions_review;

import java.util.Set;
import java.util.EnumSet;

/**
 * Represents the lifecycle states of a solution review document.
 * <p>
 * This enum tracks the document through its review and approval process,
 * from initial creation to final approval and eventual obsolescence.
 * State transitions typically follow: DRAFT → SUBMITTED → CURRENT → OUTDATED
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
     * Document has been approved and is the current active version.
     * <p>
     * This represents the approved, official version of the solution review
     * that should be used for decision-making and reference. Only one
     * document per solution should typically be in CURRENT state at a time.
     * 
     * @see SolutionReview#markAsOutdated()
     * @see SolutionReview#unApproveCurrent()
     */
    CURRENT,

    /**
     * Document was previously current but has been superseded.
     * <p>
     * When a new version of a solution review is approved, the previous
     * CURRENT document transitions to OUTDATED. This state preserves
     * historical versions while clearly indicating they should not be
     * used for current decision-making.
     * 
     * @see SolutionReview#resetAsCurrent()
     */
    OUTDATED;

    // Define valid transitions for each state
    private static final Set<DocumentState> DRAFT_TRANSITIONS = EnumSet.of(SUBMITTED);
    private static final Set<DocumentState> SUBMITTED_TRANSITIONS = EnumSet.of(CURRENT, DRAFT);
    private static final Set<DocumentState> CURRENT_TRANSITIONS = EnumSet.of(OUTDATED, SUBMITTED);
    private static final Set<DocumentState> OUTDATED_TRANSITIONS = EnumSet.of(CURRENT);

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
            case CURRENT:
                return CURRENT_TRANSITIONS;
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
     * Custom exception for invalid state transitions.
     */
    public static class IllegalStateTransitionException extends RuntimeException {
        public IllegalStateTransitionException(String message) {
            super(message);
        }

        public IllegalStateTransitionException(String message, Throwable cause) {
            super(message, cause);
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
            case CURRENT:
                return "Document is approved and represents the current active version";
            case OUTDATED:
                return "Document was previously current but has been superseded";
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
     * Checks if this state represents an active document.
     * 
     * @return true if the document is active (CURRENT or SUBMITTED)
     */
    public boolean isActive() {
        return this == CURRENT || this == SUBMITTED;
    }

    /**
     * Checks if this state represents a finalized document.
     * 
     * @return true if the document is in a final state (CURRENT or OUTDATED)
     */
    public boolean isFinalized() {
        return this == CURRENT || this == OUTDATED;
    }
}
