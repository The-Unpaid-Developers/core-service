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
        assertEquals(4, states.length);

        // Verify all expected states exist
        assertNotNull(DocumentState.valueOf("DRAFT"));
        assertNotNull(DocumentState.valueOf("SUBMITTED"));
        assertNotNull(DocumentState.valueOf("CURRENT"));
        assertNotNull(DocumentState.valueOf("OUTDATED"));
    }

    // Tests for canTransitionTo() method
    @Test
    @DisplayName("DRAFT can transition to SUBMITTED only")
    void draftCanTransitionToSubmittedOnly() {
        DocumentState draft = DocumentState.DRAFT;

        assertTrue(draft.canTransitionTo(DocumentState.SUBMITTED));
        assertFalse(draft.canTransitionTo(DocumentState.CURRENT));
        assertFalse(draft.canTransitionTo(DocumentState.OUTDATED));
        assertFalse(draft.canTransitionTo(DocumentState.DRAFT));
    }

    @Test
    @DisplayName("SUBMITTED can transition to CURRENT and DRAFT")
    void submittedCanTransitionToCurrentAndDraft() {
        DocumentState submitted = DocumentState.SUBMITTED;

        assertTrue(submitted.canTransitionTo(DocumentState.CURRENT));
        assertTrue(submitted.canTransitionTo(DocumentState.DRAFT));
        assertFalse(submitted.canTransitionTo(DocumentState.OUTDATED));
        assertFalse(submitted.canTransitionTo(DocumentState.SUBMITTED));
    }

    @Test
    @DisplayName("CURRENT can transition to OUTDATED and SUBMITTED")
    void currentCanTransitionToOutdatedAndSubmitted() {
        DocumentState current = DocumentState.CURRENT;

        assertTrue(current.canTransitionTo(DocumentState.OUTDATED));
        assertTrue(current.canTransitionTo(DocumentState.SUBMITTED));
        assertFalse(current.canTransitionTo(DocumentState.DRAFT));
        assertFalse(current.canTransitionTo(DocumentState.CURRENT));
    }

    @Test
    @DisplayName("OUTDATED can transition to CURRENT only")
    void outdatedCanTransitionToCurrentOnly() {
        DocumentState outdated = DocumentState.OUTDATED;

        assertTrue(outdated.canTransitionTo(DocumentState.CURRENT));
        assertFalse(outdated.canTransitionTo(DocumentState.DRAFT));
        assertFalse(outdated.canTransitionTo(DocumentState.SUBMITTED));
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
        assertFalse(validTransitions.contains(DocumentState.CURRENT));
        assertFalse(validTransitions.contains(DocumentState.OUTDATED));
    }

    @Test
    @DisplayName("SUBMITTED should have valid transitions to CURRENT and DRAFT")
    void submittedShouldHaveValidTransitionsToCurrentAndDraft() {
        Set<DocumentState> validTransitions = DocumentState.SUBMITTED.getValidTransitions();

        assertEquals(2, validTransitions.size());
        assertTrue(validTransitions.contains(DocumentState.CURRENT));
        assertTrue(validTransitions.contains(DocumentState.DRAFT));
        assertFalse(validTransitions.contains(DocumentState.SUBMITTED));
        assertFalse(validTransitions.contains(DocumentState.OUTDATED));
    }

    @Test
    @DisplayName("CURRENT should have valid transitions to OUTDATED and SUBMITTED")
    void currentShouldHaveValidTransitionsToOutdatedAndSubmitted() {
        Set<DocumentState> validTransitions = DocumentState.CURRENT.getValidTransitions();

        assertEquals(2, validTransitions.size());
        assertTrue(validTransitions.contains(DocumentState.OUTDATED));
        assertTrue(validTransitions.contains(DocumentState.SUBMITTED));
        assertFalse(validTransitions.contains(DocumentState.DRAFT));
        assertFalse(validTransitions.contains(DocumentState.CURRENT));
    }

    @Test
    @DisplayName("OUTDATED should have valid transitions to CURRENT only")
    void outdatedShouldHaveValidTransitionsToCurrent() {
        Set<DocumentState> validTransitions = DocumentState.OUTDATED.getValidTransitions();

        assertEquals(1, validTransitions.size());
        assertTrue(validTransitions.contains(DocumentState.CURRENT));
        assertFalse(validTransitions.contains(DocumentState.DRAFT));
        assertFalse(validTransitions.contains(DocumentState.SUBMITTED));
        assertFalse(validTransitions.contains(DocumentState.OUTDATED));
    }

    // Tests for validateTransition() method
    @Test
    @DisplayName("validateTransition() should pass for valid transitions")
    void validateTransitionShouldPassForValidTransitions() {
        // Test all valid transitions
        assertDoesNotThrow(() -> DocumentState.DRAFT.validateTransition(DocumentState.SUBMITTED));
        assertDoesNotThrow(() -> DocumentState.SUBMITTED.validateTransition(DocumentState.CURRENT));
        assertDoesNotThrow(() -> DocumentState.SUBMITTED.validateTransition(DocumentState.DRAFT));
        assertDoesNotThrow(() -> DocumentState.CURRENT.validateTransition(DocumentState.OUTDATED));
        assertDoesNotThrow(() -> DocumentState.CURRENT.validateTransition(DocumentState.SUBMITTED));
        assertDoesNotThrow(() -> DocumentState.OUTDATED.validateTransition(DocumentState.CURRENT));
    }

    @Test
    @DisplayName("validateTransition() should throw IllegalStateTransitionException for invalid transitions")
    void validateTransitionShouldThrowExceptionForInvalidTransitions() {
        // Test invalid transitions
        IllegalStateTransitionException exception1 = assertThrows(
                IllegalStateTransitionException.class,
                () -> DocumentState.DRAFT.validateTransition(DocumentState.CURRENT));
        assertTrue(exception1.getMessage().contains("Invalid state transition from DRAFT to CURRENT"));
        assertTrue(exception1.getMessage().contains("Valid transitions from DRAFT are: [SUBMITTED]"));

        IllegalStateTransitionException exception2 = assertThrows(
                IllegalStateTransitionException.class,
                () -> DocumentState.OUTDATED.validateTransition(DocumentState.DRAFT));
        assertTrue(exception2.getMessage().contains("Invalid state transition from OUTDATED to DRAFT"));
        assertTrue(exception2.getMessage().contains("Valid transitions from OUTDATED are: [CURRENT]"));
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
        assertEquals("Document is approved and represents the current active version",
                DocumentState.CURRENT.getDescription());
        assertEquals("Document was previously current but has been superseded",
                DocumentState.OUTDATED.getDescription());
    }

    // Tests for isEditable() method
    @Test
    @DisplayName("isEditable() should return true only for DRAFT")
    void isEditableShouldReturnTrueOnlyForDraft() {
        assertTrue(DocumentState.DRAFT.isEditable());
        assertFalse(DocumentState.SUBMITTED.isEditable());
        assertFalse(DocumentState.CURRENT.isEditable());
        assertFalse(DocumentState.OUTDATED.isEditable());
    }

    // Tests for isActive() method
    @Test
    @DisplayName("isActive() should return true for CURRENT and SUBMITTED")
    void isActiveShouldReturnTrueForCurrentAndSubmitted() {
        assertFalse(DocumentState.DRAFT.isActive());
        assertTrue(DocumentState.SUBMITTED.isActive());
        assertTrue(DocumentState.CURRENT.isActive());
        assertFalse(DocumentState.OUTDATED.isActive());
    }

    // Tests for isFinalized() method
    @Test
    @DisplayName("isFinalized() should return true for CURRENT and OUTDATED")
    void isFinalizedShouldReturnTrueForCurrentAndOutdated() {
        assertFalse(DocumentState.DRAFT.isFinalized());
        assertFalse(DocumentState.SUBMITTED.isFinalized());
        assertTrue(DocumentState.CURRENT.isFinalized());
        assertTrue(DocumentState.OUTDATED.isFinalized());
    }

    // Integration tests for state transition scenarios
    @Test
    @DisplayName("Should support complete document lifecycle")
    void shouldSupportCompleteDocumentLifecycle() {
        // Test typical lifecycle: DRAFT -> SUBMITTED -> CURRENT -> OUTDATED

        // DRAFT to SUBMITTED
        assertTrue(DocumentState.DRAFT.canTransitionTo(DocumentState.SUBMITTED));
        assertDoesNotThrow(() -> DocumentState.DRAFT.validateTransition(DocumentState.SUBMITTED));

        // SUBMITTED to CURRENT
        assertTrue(DocumentState.SUBMITTED.canTransitionTo(DocumentState.CURRENT));
        assertDoesNotThrow(() -> DocumentState.SUBMITTED.validateTransition(DocumentState.CURRENT));

        // CURRENT to OUTDATED
        assertTrue(DocumentState.CURRENT.canTransitionTo(DocumentState.OUTDATED));
        assertDoesNotThrow(() -> DocumentState.CURRENT.validateTransition(DocumentState.OUTDATED));
    }

    @Test
    @DisplayName("Should support rejection scenarios")
    void shouldSupportRejectionScenarios() {
        // Test rejection: SUBMITTED -> DRAFT
        assertTrue(DocumentState.SUBMITTED.canTransitionTo(DocumentState.DRAFT));
        assertDoesNotThrow(() -> DocumentState.SUBMITTED.validateTransition(DocumentState.DRAFT));

        // Test un-approval: CURRENT -> SUBMITTED
        assertTrue(DocumentState.CURRENT.canTransitionTo(DocumentState.SUBMITTED));
        assertDoesNotThrow(() -> DocumentState.CURRENT.validateTransition(DocumentState.SUBMITTED));

        // OUTDATED back to CURRENT (reset scenario)
        assertTrue(DocumentState.OUTDATED.canTransitionTo(DocumentState.CURRENT));
        assertDoesNotThrow(() -> DocumentState.OUTDATED.validateTransition(DocumentState.CURRENT));
    }

    @Test
    @DisplayName("State behavior should be consistent across all utility methods")
    void stateBehaviorShouldBeConsistentAcrossUtilityMethods() {
        // DRAFT: editable, not active, not finalized
        DocumentState draft = DocumentState.DRAFT;
        assertTrue(draft.isEditable());
        assertFalse(draft.isActive());
        assertFalse(draft.isFinalized());

        // SUBMITTED: not editable, active, not finalized
        DocumentState submitted = DocumentState.SUBMITTED;
        assertFalse(submitted.isEditable());
        assertTrue(submitted.isActive());
        assertFalse(submitted.isFinalized());

        // CURRENT: not editable, active, finalized
        DocumentState current = DocumentState.CURRENT;
        assertFalse(current.isEditable());
        assertTrue(current.isActive());
        assertTrue(current.isFinalized());

        // OUTDATED: not editable, not active, finalized
        DocumentState outdated = DocumentState.OUTDATED;
        assertFalse(outdated.isEditable());
        assertFalse(outdated.isActive());
        assertTrue(outdated.isFinalized());
    }

    @Test
    @DisplayName("toString() should return state name")
    void toStringShouldReturnStateName() {
        assertEquals("DRAFT", DocumentState.DRAFT.toString());
        assertEquals("SUBMITTED", DocumentState.SUBMITTED.toString());
        assertEquals("CURRENT", DocumentState.CURRENT.toString());
        assertEquals("OUTDATED", DocumentState.OUTDATED.toString());
    }

    @Test
    @DisplayName("valueOf() should work correctly")
    void valueOfShouldWorkCorrectly() {
        assertEquals(DocumentState.DRAFT, DocumentState.valueOf("DRAFT"));
        assertEquals(DocumentState.SUBMITTED, DocumentState.valueOf("SUBMITTED"));
        assertEquals(DocumentState.CURRENT, DocumentState.valueOf("CURRENT"));
        assertEquals(DocumentState.OUTDATED, DocumentState.valueOf("OUTDATED"));

        assertThrows(IllegalArgumentException.class, () -> DocumentState.valueOf("INVALID"));
        assertThrows(NullPointerException.class, () -> DocumentState.valueOf(null));
    }

    @Test
    @DisplayName("ordinal() should return correct values")
    void ordinalShouldReturnCorrectValues() {
        assertEquals(0, DocumentState.DRAFT.ordinal());
        assertEquals(1, DocumentState.SUBMITTED.ordinal());
        assertEquals(2, DocumentState.CURRENT.ordinal());
        assertEquals(3, DocumentState.OUTDATED.ordinal());
    }

    @Test
    @DisplayName("name() should return state name")
    void nameShouldReturnStateName() {
        assertEquals("DRAFT", DocumentState.DRAFT.name());
        assertEquals("SUBMITTED", DocumentState.SUBMITTED.name());
        assertEquals("CURRENT", DocumentState.CURRENT.name());
        assertEquals("OUTDATED", DocumentState.OUTDATED.name());
    }

    // Tests for StateOperation functionality
    @Test
    @DisplayName("StateOperation should have correct operation names")
    void stateOperationShouldHaveCorrectOperationNames() {
        assertEquals("submit document", DocumentState.StateOperation.SUBMIT.getOperationName());
        assertEquals("remove submission", DocumentState.StateOperation.REMOVE_SUBMISSION.getOperationName());
        assertEquals("approve document", DocumentState.StateOperation.APPROVE.getOperationName());
        assertEquals("un-approve document", DocumentState.StateOperation.UNAPPROVE.getOperationName());
        assertEquals("mark as outdated", DocumentState.StateOperation.MARK_OUTDATED.getOperationName());
        assertEquals("reset as current", DocumentState.StateOperation.RESET_CURRENT.getOperationName());
    }

    @Test
    @DisplayName("StateOperation should have correct target states")
    void stateOperationShouldHaveCorrectTargetStates() {
        assertEquals(DocumentState.SUBMITTED, DocumentState.StateOperation.SUBMIT.getTargetState());
        assertEquals(DocumentState.DRAFT, DocumentState.StateOperation.REMOVE_SUBMISSION.getTargetState());
        assertEquals(DocumentState.CURRENT, DocumentState.StateOperation.APPROVE.getTargetState());
        assertEquals(DocumentState.SUBMITTED, DocumentState.StateOperation.UNAPPROVE.getTargetState());
        assertEquals(DocumentState.OUTDATED, DocumentState.StateOperation.MARK_OUTDATED.getTargetState());
        assertEquals(DocumentState.CURRENT, DocumentState.StateOperation.RESET_CURRENT.getTargetState());
    }

    @Test
    @DisplayName("canExecuteOperation should return correct results for each state")
    void canExecuteOperationShouldReturnCorrectResultsForEachState() {
        // DRAFT can only SUBMIT
        assertTrue(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));
        assertFalse(DocumentState.DRAFT.canExecuteOperation(DocumentState.StateOperation.RESET_CURRENT));

        // SUBMITTED can REMOVE_SUBMISSION and APPROVE
        assertFalse(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        assertTrue(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertTrue(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        assertFalse(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        assertFalse(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));
        assertFalse(DocumentState.SUBMITTED.canExecuteOperation(DocumentState.StateOperation.RESET_CURRENT));

        // CURRENT can UNAPPROVE and MARK_OUTDATED
        assertFalse(DocumentState.CURRENT.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        assertFalse(DocumentState.CURRENT.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertFalse(DocumentState.CURRENT.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        assertTrue(DocumentState.CURRENT.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        assertTrue(DocumentState.CURRENT.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));
        assertFalse(DocumentState.CURRENT.canExecuteOperation(DocumentState.StateOperation.RESET_CURRENT));

        // OUTDATED can only RESET_CURRENT
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.SUBMIT));
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        assertFalse(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));
        assertTrue(DocumentState.OUTDATED.canExecuteOperation(DocumentState.StateOperation.RESET_CURRENT));
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

        var currentOperations = DocumentState.CURRENT.getAvailableOperations();
        assertEquals(2, currentOperations.size());
        assertTrue(currentOperations.contains(DocumentState.StateOperation.UNAPPROVE));
        assertTrue(currentOperations.contains(DocumentState.StateOperation.MARK_OUTDATED));

        var outdatedOperations = DocumentState.OUTDATED.getAvailableOperations();
        assertEquals(1, outdatedOperations.size());
        assertTrue(outdatedOperations.contains(DocumentState.StateOperation.RESET_CURRENT));
    }

    @Test
    @DisplayName("executeOperation should execute valid operations correctly")
    void executeOperationShouldExecuteValidOperationsCorrectly() {
        assertEquals(DocumentState.SUBMITTED,
                DocumentState.DRAFT.executeOperation(DocumentState.StateOperation.SUBMIT));
        assertEquals(DocumentState.DRAFT,
                DocumentState.SUBMITTED.executeOperation(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertEquals(DocumentState.CURRENT,
                DocumentState.SUBMITTED.executeOperation(DocumentState.StateOperation.APPROVE));
        assertEquals(DocumentState.SUBMITTED,
                DocumentState.CURRENT.executeOperation(DocumentState.StateOperation.UNAPPROVE));
        assertEquals(DocumentState.OUTDATED,
                DocumentState.CURRENT.executeOperation(DocumentState.StateOperation.MARK_OUTDATED));
        assertEquals(DocumentState.CURRENT,
                DocumentState.OUTDATED.executeOperation(DocumentState.StateOperation.RESET_CURRENT));
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
                () -> DocumentState.CURRENT.executeOperation(DocumentState.StateOperation.SUBMIT));
        assertTrue(exception2.getMessage().contains("Cannot submit document"));
        assertTrue(exception2.getMessage().contains("document state is CURRENT but must be DRAFT"));

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
        assertNotNull(DocumentState.StateOperation.valueOf("UNAPPROVE"));
        assertNotNull(DocumentState.StateOperation.valueOf("MARK_OUTDATED"));
        assertNotNull(DocumentState.StateOperation.valueOf("RESET_CURRENT"));
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

        // SUBMITTED -> CURRENT
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.APPROVE));
        state = state.executeOperation(DocumentState.StateOperation.APPROVE);
        assertEquals(DocumentState.CURRENT, state);

        // CURRENT -> OUTDATED
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.MARK_OUTDATED));
        state = state.executeOperation(DocumentState.StateOperation.MARK_OUTDATED);
        assertEquals(DocumentState.OUTDATED, state);

        // OUTDATED -> CURRENT
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.RESET_CURRENT));
        state = state.executeOperation(DocumentState.StateOperation.RESET_CURRENT);
        assertEquals(DocumentState.CURRENT, state);
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

        // Back to SUBMITTED and then CURRENT
        state = state.executeOperation(DocumentState.StateOperation.SUBMIT);
        state = state.executeOperation(DocumentState.StateOperation.APPROVE);
        assertEquals(DocumentState.CURRENT, state);

        // CURRENT -> SUBMITTED (un-approval)
        assertTrue(state.canExecuteOperation(DocumentState.StateOperation.UNAPPROVE));
        state = state.executeOperation(DocumentState.StateOperation.UNAPPROVE);
        assertEquals(DocumentState.SUBMITTED, state);
    }
}
