package com.project.core_service.models.solutions_review;

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
     * @see SolutionReview#unapproveCurrent()
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
    OUTDATED
}
