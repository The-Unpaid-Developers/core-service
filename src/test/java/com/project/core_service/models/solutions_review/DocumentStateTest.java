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
}
