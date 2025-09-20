package com.project.core_service.models.solutions_review;

import com.project.core_service.exceptions.IllegalStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DocumentState enum.
 * Tests all state transition logic, validation methods, and utility functions.
 */
class DocumentStateTest {

    @Test
    @DisplayName("Should have all expected states defined")
    void shouldHaveAllExpectedStates() {
        DocumentState[] states = DocumentState.values();
        assertEquals(5, states.length);

        // Verify all expected states exist
        assertNotNull(DocumentState.valueOf("DRAFT"));
        assertNotNull(DocumentState.valueOf("SUBMITTED"));
        assertNotNull(DocumentState.valueOf("APPROVED"));
        assertNotNull(DocumentState.valueOf("ACTIVE"));
        assertNotNull(DocumentState.valueOf("OUTDATED"));
    }

    // Tests for canTransitionTo() method
    @Test
    @DisplayName("DRAFT can transition to SUBMITTED only")
    void draftCanTransitionToSubmittedOnly() {
        DocumentState draft = DocumentState.DRAFT;

        assertTrue(draft.canTransitionTo(DocumentState.SUBMITTED));
        assertFalse(draft.canTransitionTo(DocumentState.APPROVED));
        assertFalse(draft.canTransitionTo(DocumentState.ACTIVE));
        assertFalse(draft.canTransitionTo(DocumentState.OUTDATED));
        assertFalse(draft.canTransitionTo(DocumentState.DRAFT));
    }

    @Test
    @DisplayName("SUBMITTED can transition to APPROVED and DRAFT")
    void submittedCanTransitionToApprovedAndDraft() {
        DocumentState submitted = DocumentState.SUBMITTED;

        assertTrue(submitted.canTransitionTo(DocumentState.APPROVED));
        assertTrue(submitted.canTransitionTo(DocumentState.DRAFT));
        assertFalse(submitted.canTransitionTo(DocumentState.ACTIVE));
        assertFalse(submitted.canTransitionTo(DocumentState.OUTDATED));
        assertFalse(submitted.canTransitionTo(DocumentState.SUBMITTED));
    }

    @Test
    @DisplayName("APPROVED can transition to ACTIVE only")
    void approvedCanTransitionToActiveOnly() {
        DocumentState approved = DocumentState.APPROVED;

        assertTrue(approved.canTransitionTo(DocumentState.ACTIVE));
        assertFalse(approved.canTransitionTo(DocumentState.DRAFT));
        assertFalse(approved.canTransitionTo(DocumentState.SUBMITTED));
        assertFalse(approved.canTransitionTo(DocumentState.OUTDATED));
        assertFalse(approved.canTransitionTo(DocumentState.APPROVED));
    }

    @Test
    @DisplayName("ACTIVE can transition to OUTDATED only")
    void activeCanTransitionToOutdatedOnly() {
        DocumentState active = DocumentState.ACTIVE;

        assertTrue(active.canTransitionTo(DocumentState.OUTDATED));
        assertFalse(active.canTransitionTo(DocumentState.DRAFT));
        assertFalse(active.canTransitionTo(DocumentState.SUBMITTED));
        assertFalse(active.canTransitionTo(DocumentState.APPROVED));
        assertFalse(active.canTransitionTo(DocumentState.ACTIVE));
    }

    @Test
    @DisplayName("OUTDATED cannot transition to any state")
    void outdatedCannotTransitionToAnyState() {
        DocumentState outdated = DocumentState.OUTDATED;

        assertFalse(outdated.canTransitionTo(DocumentState.DRAFT));
        assertFalse(outdated.canTransitionTo(DocumentState.SUBMITTED));
        assertFalse(outdated.canTransitionTo(DocumentState.APPROVED));
        assertFalse(outdated.canTransitionTo(DocumentState.ACTIVE));
        assertFalse(outdated.canTransitionTo(DocumentState.OUTDATED));
    }

    @Test
    @DisplayName("canTransitionTo() should throw IllegalArgumentException for null target")
    void canTransitionToShouldThrowExceptionForNullTarget() {
        DocumentState draft = DocumentState.DRAFT;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> draft.canTransitionTo(null));
        assertEquals("Target state cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(DocumentState.class)
    @DisplayName("canTransitionTo() should return false for same state")
    void canTransitionToShouldReturnFalseForSameState(DocumentState state) {
        assertFalse(state.canTransitionTo(state));
    }

    // Tests for getValidTransitions() method
    @Test
    @DisplayName("DRAFT should have valid transitions to SUBMITTED only")
    void draftShouldHaveValidTransitionsToSubmittedOnly() {
        Set<DocumentState> validTransitions = DocumentState.DRAFT.getValidTransitions();

        assertEquals(1, validTransitions.size());
        assertTrue(validTransitions.contains(DocumentState.SUBMITTED));
        assertFalse(validTransitions.contains(DocumentState.DRAFT));
        assertFalse(validTransitions.contains(DocumentState.APPROVED));
        assertFalse(validTransitions.contains(DocumentState.ACTIVE));
        assertFalse(validTransitions.contains(DocumentState.OUTDATED));
    }

    @Test
    @DisplayName("SUBMITTED should have valid transitions to APPROVED and DRAFT")
    void submittedShouldHaveValidTransitionsToApprovedAndDraft() {
        Set<DocumentState> validTransitions = DocumentState.SUBMITTED.getValidTransitions();

        assertEquals(2, validTransitions.size());
        assertTrue(validTransitions.contains(DocumentState.APPROVED));
        assertTrue(validTransitions.contains(DocumentState.DRAFT));
        assertFalse(validTransitions.contains(DocumentState.SUBMITTED));
        assertFalse(validTransitions.contains(DocumentState.ACTIVE));
        assertFalse(validTransitions.contains(DocumentState.OUTDATED));
    }

    @Test
    @DisplayName("APPROVED should have valid transitions to ACTIVE only")
    void approvedShouldHaveValidTransitionsToActiveOnly() {
        Set<DocumentState> validTransitions = DocumentState.APPROVED.getValidTransitions();

        assertEquals(1, validTransitions.size());
        assertTrue(validTransitions.contains(DocumentState.ACTIVE));
        assertFalse(validTransitions.contains(DocumentState.DRAFT));
        assertFalse(validTransitions.contains(DocumentState.SUBMITTED));
        assertFalse(validTransitions.contains(DocumentState.APPROVED));
        assertFalse(validTransitions.contains(DocumentState.OUTDATED));
    }

    @Test
    @DisplayName("ACTIVE should have valid transitions to OUTDATED only")
    void activeShouldHaveValidTransitionsToOutdatedOnly() {
        Set<DocumentState> validTransitions = DocumentState.ACTIVE.getValidTransitions();

        assertEquals(1, validTransitions.size());
        assertTrue(validTransitions.contains(DocumentState.OUTDATED));
        assertFalse(validTransitions.contains(DocumentState.DRAFT));
        assertFalse(validTransitions.contains(DocumentState.SUBMITTED));
        assertFalse(validTransitions.contains(DocumentState.APPROVED));
        assertFalse(validTransitions.contains(DocumentState.ACTIVE));
    }

    @Test
    @DisplayName("OUTDATED should have no valid transitions")
    void outdatedShouldHaveNoValidTransitions() {
        Set<DocumentState> validTransitions = DocumentState.OUTDATED.getValidTransitions();

        assertEquals(0, validTransitions.size());
        assertFalse(validTransitions.contains(DocumentState.DRAFT));
        assertFalse(validTransitions.contains(DocumentState.SUBMITTED));
        assertFalse(validTransitions.contains(DocumentState.APPROVED));
        assertFalse(validTransitions.contains(DocumentState.ACTIVE));
        assertFalse(validTransitions.contains(DocumentState.OUTDATED));
    }

    // Tests for validateTransition() method
    @Test
    @DisplayName("validateTransition() should pass for valid transitions")
    void validateTransitionShouldPassForValidTransitions() {
        // Test all valid transitions
        assertDoesNotThrow(() -> DocumentState.DRAFT.validateTransition(DocumentState.SUBMITTED));
        assertDoesNotThrow(() -> DocumentState.SUBMITTED.validateTransition(DocumentState.APPROVED));
        assertDoesNotThrow(() -> DocumentState.SUBMITTED.validateTransition(DocumentState.DRAFT));
        assertDoesNotThrow(() -> DocumentState.APPROVED.validateTransition(DocumentState.ACTIVE));
        assertDoesNotThrow(() -> DocumentState.ACTIVE.validateTransition(DocumentState.OUTDATED));
    }

    @Test
    @DisplayName("validateTransition() should throw IllegalStateTransitionException for invalid transitions")
    void validateTransitionShouldThrowExceptionForInvalidTransitions() {
        // Test invalid transitions
        IllegalStateTransitionException exception1 = assertThrows(
                IllegalStateTransitionException.class,
                () -> DocumentState.DRAFT.validateTransition(DocumentState.ACTIVE));
        assertTrue(exception1.getMessage().contains("Invalid state transition from DRAFT to ACTIVE"));
        assertTrue(exception1.getMessage().contains("Valid transitions from DRAFT are: [SUBMITTED]"));

        IllegalStateTransitionException exception2 = assertThrows(
                IllegalStateTransitionException.class,
                () -> DocumentState.OUTDATED.validateTransition(DocumentState.DRAFT));
        assertTrue(exception2.getMessage().contains("Invalid state transition from OUTDATED to DRAFT"));
        assertTrue(exception2.getMessage().contains("Valid transitions from OUTDATED are: []"));
    }

    @Test
    @DisplayName("validateTransition() should throw IllegalStateTransitionException for same state")
    void validateTransitionShouldThrowExceptionForSameState() {
        IllegalStateTransitionException exception = assertThrows(
                IllegalStateTransitionException.class,
                () -> DocumentState.DRAFT.validateTransition(DocumentState.DRAFT));
        assertEquals("Document is already in DRAFT state", exception.getMessage());
    }

    @Test
    @DisplayName("validateTransition() should throw IllegalArgumentException for null target")
    void validateTransitionShouldThrowExceptionForNullTarget() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DocumentState.DRAFT.validateTransition(null));
        assertEquals("Target state cannot be null", exception.getMessage());
    }

    // Tests for getDescription() method
    @Test
    @DisplayName("getDescription() should return correct descriptions for all states")
    void getDescriptionShouldReturnCorrectDescriptions() {
        assertEquals("Document is being edited and is not ready for review",
                DocumentState.DRAFT.getDescription());
        assertEquals("Document has been submitted for review and approval",
                DocumentState.SUBMITTED.getDescription());
        assertEquals("Document has been approved and is ready to be activated",
                DocumentState.APPROVED.getDescription());
        assertEquals("Document is approved and represents the current active version",
                DocumentState.ACTIVE.getDescription());
        assertEquals("Document was previously active but has been superseded",
                DocumentState.OUTDATED.getDescription());
    }

    // Tests for isEditable() method
    @Test
    @DisplayName("isEditable() should return true only for DRAFT")
    void isEditableShouldReturnTrueOnlyForDraft() {
        assertTrue(DocumentState.DRAFT.isEditable());
        assertFalse(DocumentState.SUBMITTED.isEditable());
        assertFalse(DocumentState.APPROVED.isEditable());
        assertFalse(DocumentState.ACTIVE.isEditable());
        assertFalse(DocumentState.OUTDATED.isEditable());
    }

    // Tests for isFinalized() method
    @Test
    @DisplayName("isFinalized() should return true for APPROVED, ACTIVE and OUTDATED")
    void isFinalizedShouldReturnTrueForApprovedActiveAndOutdated() {
        assertFalse(DocumentState.DRAFT.isFinalized());
        assertFalse(DocumentState.SUBMITTED.isFinalized());
        assertFalse(DocumentState.APPROVED.isFinalized());
        assertTrue(DocumentState.ACTIVE.isFinalized());
        assertTrue(DocumentState.OUTDATED.isFinalized());
    }

    // Integration tests for state transition scenarios
    @Test
    @DisplayName("Should support complete document lifecycle")
    void shouldSupportCompleteDocumentLifecycle() {
        // Test typical lifecycle: DRAFT -> SUBMITTED -> APPROVED -> ACTIVE -> OUTDATED

        // DRAFT to SUBMITTED
        assertTrue(DocumentState.DRAFT.canTransitionTo(DocumentState.SUBMITTED));
        assertDoesNotThrow(() -> DocumentState.DRAFT.validateTransition(DocumentState.SUBMITTED));

        // SUBMITTED to APPROVED
        assertTrue(DocumentState.SUBMITTED.canTransitionTo(DocumentState.APPROVED));
        assertDoesNotThrow(() -> DocumentState.SUBMITTED.validateTransition(DocumentState.APPROVED));

        // APPROVED to ACTIVE
        assertTrue(DocumentState.APPROVED.canTransitionTo(DocumentState.ACTIVE));
        assertDoesNotThrow(() -> DocumentState.APPROVED.validateTransition(DocumentState.ACTIVE));

        // ACTIVE to OUTDATED
        assertTrue(DocumentState.ACTIVE.canTransitionTo(DocumentState.OUTDATED));
        assertDoesNotThrow(() -> DocumentState.ACTIVE.validateTransition(DocumentState.OUTDATED));
    }

    @Test
    @DisplayName("Should support rejection scenarios")
    void shouldSupportRejectionScenarios() {
        // Test rejection: SUBMITTED -> DRAFT
        assertTrue(DocumentState.SUBMITTED.canTransitionTo(DocumentState.DRAFT));
        assertDoesNotThrow(() -> DocumentState.SUBMITTED.validateTransition(DocumentState.DRAFT));

        // Note: Based on new model, ACTIVE and OUTDATED don't have backward transitions
        // ACTIVE can only go to OUTDATED, OUTDATED is terminal
    }

    @Test
    @DisplayName("State behavior should be consistent across all utility methods")
    void stateBehaviorShouldBeConsistentAcrossUtilityMethods() {
        // DRAFT: editable, not finalized
        DocumentState draft = DocumentState.DRAFT;
        assertTrue(draft.isEditable());
        assertFalse(draft.isFinalized());

        // SUBMITTED: not editable, not finalized
        DocumentState submitted = DocumentState.SUBMITTED;
        assertFalse(submitted.isEditable());
        assertFalse(submitted.isFinalized());

        // APPROVED: not editable, finalized
        DocumentState approved = DocumentState.APPROVED;
        assertFalse(approved.isEditable());
        assertFalse(approved.isFinalized());

        // ACTIVE: not editable, finalized
        DocumentState active = DocumentState.ACTIVE;
        assertFalse(active.isEditable());
        assertTrue(active.isFinalized());

        // OUTDATED: not editable, finalized
        DocumentState outdated = DocumentState.OUTDATED;
        assertFalse(outdated.isEditable());
        assertTrue(outdated.isFinalized());
    }

    @Test
    @DisplayName("toString() should return state name")
    void toStringShouldReturnStateName() {
        assertEquals("DRAFT", DocumentState.DRAFT.toString());
        assertEquals("SUBMITTED", DocumentState.SUBMITTED.toString());
        assertEquals("APPROVED", DocumentState.APPROVED.toString());
        assertEquals("ACTIVE", DocumentState.ACTIVE.toString());
        assertEquals("OUTDATED", DocumentState.OUTDATED.toString());
    }

    @Test
    @DisplayName("valueOf() should work correctly")
    void valueOfShouldWorkCorrectly() {
        assertEquals(DocumentState.DRAFT, DocumentState.valueOf("DRAFT"));
        assertEquals(DocumentState.SUBMITTED, DocumentState.valueOf("SUBMITTED"));
        assertEquals(DocumentState.APPROVED, DocumentState.valueOf("APPROVED"));
        assertEquals(DocumentState.ACTIVE, DocumentState.valueOf("ACTIVE"));
        assertEquals(DocumentState.OUTDATED, DocumentState.valueOf("OUTDATED"));

        assertThrows(IllegalArgumentException.class, () -> DocumentState.valueOf("INVALID"));
        assertThrows(NullPointerException.class, () -> DocumentState.valueOf(null));
    }

    @Test
    @DisplayName("ordinal() should return correct values")
    void ordinalShouldReturnCorrectValues() {
        assertEquals(0, DocumentState.DRAFT.ordinal());
        assertEquals(1, DocumentState.SUBMITTED.ordinal());
        assertEquals(2, DocumentState.APPROVED.ordinal());
        assertEquals(3, DocumentState.ACTIVE.ordinal());
        assertEquals(4, DocumentState.OUTDATED.ordinal());
    }

    @Test
    @DisplayName("name() should return state name")
    void nameShouldReturnStateName() {
        assertEquals("DRAFT", DocumentState.DRAFT.name());
        assertEquals("SUBMITTED", DocumentState.SUBMITTED.name());
        assertEquals("APPROVED", DocumentState.APPROVED.name());
        assertEquals("ACTIVE", DocumentState.ACTIVE.name());
        assertEquals("OUTDATED", DocumentState.OUTDATED.name());
    }

    // Tests for StateOperation functionality
    @Test
    @DisplayName("StateOperation should have correct operation names")
    void stateOperationShouldHaveCorrectOperationNames() {
        assertEquals("submit document", DocumentState.StateOperation.SUBMIT.getOperationName());
        assertEquals("remove submission", DocumentState.StateOperation.REMOVE_SUBMISSION.getOperationName());
        assertEquals("approve document", DocumentState.StateOperation.APPROVE.getOperationName());
        assertEquals("activate document", DocumentState.StateOperation.ACTIVATE.getOperationName());
        assertEquals("un-approve document", DocumentState.StateOperation.UNAPPROVE.getOperationName());
        assertEquals("mark as outdated", DocumentState.StateOperation.MARK_OUTDATED.getOperationName());
    }

    @Test
    @DisplayName("StateOperation should have correct target states")
    void stateOperationShouldHaveCorrectTargetStates() {
        assertEquals(DocumentState.SUBMITTED, DocumentState.StateOperation.SUBMIT.getTargetState());
        assertEquals(DocumentState.DRAFT, DocumentState.StateOperation.REMOVE_SUBMISSION.getTargetState());
        assertEquals(DocumentState.APPROVED, DocumentState.StateOperation.APPROVE.getTargetState());
        assertEquals(DocumentState.ACTIVE, DocumentState.StateOperation.ACTIVATE.getTargetState());
        assertEquals(DocumentState.SUBMITTED, DocumentState.StateOperation.UNAPPROVE.getTargetState());
        assertEquals(DocumentState.OUTDATED, DocumentState.StateOperation.MARK_OUTDATED.getTargetState());
    }

    @Test
    @DisplayName("canExecuteOperation should return correct results for each state")
    void canExecuteOperationShouldReturnCorrectResultsForEachState() {
        // DRAFT can only SUBMIT
        assertTrue(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.ACTIVATE));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));

        // SUBMITTED can REMOVE_SUBMISSION and APPROVE
        assertFalse(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        assertTrue(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertTrue(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        assertFalse(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.ACTIVATE));
        assertFalse(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        assertFalse(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));

        // APPROVED can ACTIVATE and UNAPPROVE
        assertFalse(DocumentState.APPROVED.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        assertFalse(DocumentState.APPROVED.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertFalse(DocumentState.APPROVED.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        assertTrue(DocumentState.APPROVED.canExecuteOperation(DocumentState.StateOperation.ACTIVATE));
        assertTrue(DocumentState.APPROVED.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        assertFalse(DocumentState.APPROVED.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));

        // ACTIVE can only MARK_OUTDATED
        assertFalse(DocumentState.ACTIVE.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        assertFalse(DocumentState.ACTIVE.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertFalse(DocumentState.ACTIVE.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        assertFalse(DocumentState.ACTIVE.canExecuteOperation(DocumentState.StateOperation.ACTIVATE));
        assertFalse(DocumentState.ACTIVE.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        assertTrue(DocumentState.ACTIVE.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));

        // OUTDATED cannot execute any operations
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.ACTIVATE));
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));
    }

    @Test
    @DisplayName("getAvailableOperations should return correct operations for each state")
    void getAvailableOperationsShouldReturnCorrectOperationsForEachState() {
        var draftOperations = DocumentState.DRAFT.getAvailableOperations();
        assertEquals(1, draftOperations.size());
        assertTrue(draftOperations.contains(DocumentState.StateOperation.SUBMIT));

        var submittedOperations = DocumentState.SUBMITTED.getAvailableOperations();
        assertEquals(2, submittedOperations.size());
        assertTrue(submittedOperations.contains(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertTrue(submittedOperations.contains(DocumentState.StateOperation.APPROVE));

        var approvedOperations = DocumentState.APPROVED.getAvailableOperations();
        assertEquals(2, approvedOperations.size());
        assertTrue(approvedOperations.contains(DocumentState.StateOperation.ACTIVATE));
        assertTrue(approvedOperations.contains(DocumentState.StateOperation.UNAPPROVE));

        var activeOperations = DocumentState.ACTIVE.getAvailableOperations();
        assertEquals(1, activeOperations.size());
        assertTrue(activeOperations.contains(DocumentState.StateOperation.MARK_OUTDATED));

        var outdatedOperations = DocumentState.OUTDATED.getAvailableOperations();
        assertEquals(0, outdatedOperations.size());
    }

    @Test
    @DisplayName("executeOperation should execute valid operations correctly")
    void executeOperationShouldExecuteValidOperationsCorrectly() {
        assertEquals(DocumentState.SUBMITTED,
                DocumentState.DRAFT.executeOperation(DocumentState.StateOperation.SUBMIT));
        assertEquals(DocumentState.DRAFT,
                DocumentState.SUBMITTED.executeOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertEquals(DocumentState.APPROVED,
                DocumentState.SUBMITTED.executeOperation(DocumentState.StateOperation.APPROVE));
        assertEquals(DocumentState.ACTIVE,
                DocumentState.APPROVED.executeOperation(DocumentState.StateOperation.ACTIVATE));
        assertEquals(DocumentState.SUBMITTED,
                DocumentState.APPROVED.executeOperation(DocumentState.StateOperation.UNAPPROVE));
        assertEquals(DocumentState.OUTDATED,
                DocumentState.ACTIVE.executeOperation(DocumentState.StateOperation.MARK_OUTDATED));
    }

    @Test
    @DisplayName("executeOperation should throw exception for invalid operations")
    void executeOperationShouldThrowExceptionForInvalidOperations() {
        IllegalStateTransitionException exception1 = assertThrows(
                IllegalStateTransitionException.class,
                () -> DocumentState.DRAFT.executeOperation(DocumentState.StateOperation.APPROVE));
        assertTrue(exception1.getMessage().contains("Cannot approve document"));
        assertTrue(exception1.getMessage().contains("document state is DRAFT but must be SUBMITTED"));

        IllegalStateTransitionException exception2 = assertThrows(
                IllegalStateTransitionException.class,
                () -> DocumentState.ACTIVE.executeOperation(DocumentState.StateOperation.SUBMIT));
        assertTrue(exception2.getMessage().contains("Cannot submit document"));
        assertTrue(exception2.getMessage().contains("document state is ACTIVE but must be DRAFT"));

        IllegalStateTransitionException exception3 = assertThrows(
                IllegalStateTransitionException.class,
                () -> DocumentState.OUTDATED.executeOperation(DocumentState.StateOperation.APPROVE));
        assertTrue(exception3.getMessage().contains("Cannot approve document"));
        assertTrue(exception3.getMessage().contains("document state is OUTDATED but must be SUBMITTED"));
    }

    @Test
    @DisplayName("StateOperation enum should have all expected values")
    void stateOperationEnumShouldHaveAllExpectedValues() {
        DocumentState.StateOperation[] operations = DocumentState.StateOperation.values();
        assertEquals(6, operations.length);

        // Verify all expected operations exist
        assertNotNull(DocumentState.StateOperation.valueOf("SUBMIT"));
        assertNotNull(DocumentState.StateOperation.valueOf("REMOVE_SUBMISSION"));
        assertNotNull(DocumentState.StateOperation.valueOf("APPROVE"));
        assertNotNull(DocumentState.StateOperation.valueOf("ACTIVATE"));
        assertNotNull(DocumentState.StateOperation.valueOf("UNAPPROVE"));
        assertNotNull(DocumentState.StateOperation.valueOf("MARK_OUTDATED"));
    }

    @Test
    @DisplayName("Should support complete operation-based lifecycle")
    void shouldSupportCompleteOperationBasedLifecycle() {
        // Test complete lifecycle using operations
        DocumentState state = DocumentState.DRAFT;

        // DRAFT -> SUBMITTED
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        state = state.executeOperation(DocumentState.StateOperation.SUBMIT);
        assertEquals(DocumentState.SUBMITTED, state);

        // SUBMITTED -> APPROVED
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        state = state.executeOperation(DocumentState.StateOperation.APPROVE);
        assertEquals(DocumentState.APPROVED, state);

        // APPROVED -> ACTIVE
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.ACTIVATE));
        state = state.executeOperation(DocumentState.StateOperation.ACTIVATE);
        assertEquals(DocumentState.ACTIVE, state);

        // ACTIVE -> OUTDATED
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));
        state = state.executeOperation(DocumentState.StateOperation.MARK_OUTDATED);
        assertEquals(DocumentState.OUTDATED, state);

        // OUTDATED is terminal - no further operations possible
        assertEquals(0, state.getAvailableOperations().size());
    }

    @Test
    @DisplayName("Should support rejection scenarios using operations")
    void shouldSupportRejectionScenariosUsingOperations() {
        DocumentState state = DocumentState.DRAFT;

        // DRAFT -> SUBMITTED
        state = state.executeOperation(DocumentState.StateOperation.SUBMIT);
        assertEquals(DocumentState.SUBMITTED, state);

        // SUBMITTED -> DRAFT (rejection)
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        state = state.executeOperation(DocumentState.StateOperation.REMOVE_SUBMISSION);
        assertEquals(DocumentState.DRAFT, state);

        // Back to SUBMITTED and then APPROVED
        state = state.executeOperation(DocumentState.StateOperation.SUBMIT);
        state = state.executeOperation(DocumentState.StateOperation.APPROVE);
        assertEquals(DocumentState.APPROVED, state);

        // APPROVED -> SUBMITTED (un-approval)
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        state = state.executeOperation(DocumentState.StateOperation.UNAPPROVE);
        assertEquals(DocumentState.SUBMITTED, state);
    }
}
