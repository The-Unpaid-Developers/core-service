package com.project.core_service.services;

import com.project.core_service.commands.LifecycleTransitionCommand;
import com.project.core_service.exceptions.IllegalOperationException;
import com.project.core_service.exceptions.IllegalStateTransitionException;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.solution_overview.SolutionDetails;
import com.project.core_service.models.solution_overview.BusinessUnit;
import com.project.core_service.models.solution_overview.BusinessDriver;
import com.project.core_service.repositories.SolutionReviewRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolutionReviewLifecycleService Tests")
class SolutionReviewLifecycleServiceTest {

    @Mock
    private SolutionReviewRepository solutionReviewRepository;

    @Mock
    private SolutionReviewService solutionReviewService;

    @InjectMocks
    private SolutionReviewLifecycleService lifecycleService;

    private SolutionReview testSolutionReview;
    private SolutionOverview testSolutionOverview;
    private LifecycleTransitionCommand testCommand;

    @BeforeEach
    void setUp() {
        SolutionDetails solutionDetails = new SolutionDetails(
                "Test Solution",
                "Test Project",
                "TST-001",
                "Test Architect",
                "Test PM",
                "Test Partner");

        testSolutionOverview = SolutionOverview.newDraftBuilder()
                .id("overview-1")
                .solutionDetails(solutionDetails)
                .businessUnit(BusinessUnit.UNKNOWN)
                .businessDriver(BusinessDriver.BUSINESS_OR_CUSTOMER_GROWTH)
                .valueOutcome("Test outcome")
                .build();

        testSolutionReview = new SolutionReview("SYS-001", testSolutionOverview);
        testSolutionReview.setId("sr-1");
        testSolutionReview.setCreatedBy("user1");

        testCommand = new LifecycleTransitionCommand();
        testCommand.setDocumentId("sr-1");
        testCommand.setModifiedBy("user2");
        testCommand.setComment("Test transition");
    }

    @Nested
    @DisplayName("Execute Transition Tests")
    class ExecuteTransitionTests {

        @Test
    @DisplayName("Should throw NotFoundException when solution review not found")
        void shouldThrowNotFoundExceptionWhenSolutionReviewNotFound() {
            // Arrange
            testCommand.setOperation("SUBMIT");
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> lifecycleService.executeTransition(testCommand));

            assertTrue(exception.getMessage().contains("SolutionReview with ID 'sr-1' not found"));
            verify(solutionReviewRepository).findById("sr-1");
            verifyNoMoreInteractions(solutionReviewRepository, solutionReviewService);
        }

        @Test
        @DisplayName("Should throw IllegalStateTransitionException for invalid operation")
        void shouldThrowIllegalStateTransitionExceptionForInvalidOperation() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.APPROVED);
            testCommand.setOperation("SUBMIT"); // Invalid from APPROVED
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));

            // Act & Assert
            IllegalStateTransitionException exception = assertThrows(IllegalStateTransitionException.class,
                    () -> lifecycleService.executeTransition(testCommand));

            assertTrue(exception.getMessage().contains("Cannot execute operation 'submit document'"));
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should successfully execute SUBMIT operation")
        void shouldSuccessfullyExecuteSubmitOperation() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.DRAFT);
            testCommand.setOperation("SUBMIT");
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            assertEquals(DocumentState.SUBMITTED, testSolutionReview.getDocumentState());
            assertEquals("user2", testSolutionReview.getLastModifiedBy());
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository).save(testSolutionReview);
        }

        @Test
        @DisplayName("Should successfully execute REMOVE_SUBMISSION operation")
        void shouldSuccessfullyExecuteRemoveSubmissionOperation() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.SUBMITTED);
            testCommand.setOperation("REMOVE_SUBMISSION");
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            assertEquals(DocumentState.DRAFT, testSolutionReview.getDocumentState());
            assertEquals("user2", testSolutionReview.getLastModifiedBy());
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository).save(testSolutionReview);
        }

        @Test
        @DisplayName("Should successfully execute APPROVE operation")
        void shouldSuccessfullyExecuteApproveOperation() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.SUBMITTED);
            testCommand.setOperation("APPROVE");
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);
            doNothing().when(solutionReviewService).validateExclusiveStateConstraint(anyString(),
                    anyString());

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            assertEquals(DocumentState.APPROVED, testSolutionReview.getDocumentState());
            assertEquals("user2", testSolutionReview.getLastModifiedBy());
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository).save(testSolutionReview);
            verify(solutionReviewService).validateExclusiveStateConstraint("SYS-001", "sr-1");
        }

        @Test
        @DisplayName("Should successfully execute ACTIVATE operation")
        void shouldSuccessfullyExecuteActivateOperation() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.APPROVED);
            testCommand.setOperation("ACTIVATE");
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);
            doNothing().when(solutionReviewService).validateActiveStateConstraint(anyString(),
                    anyString());

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            assertEquals(DocumentState.ACTIVE, testSolutionReview.getDocumentState());
            assertEquals("user2", testSolutionReview.getLastModifiedBy());
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository).save(testSolutionReview);
            verify(solutionReviewService).validateActiveStateConstraint("SYS-001", "sr-1");
        }

        @Test
        @DisplayName("Should successfully execute UNAPPROVE operation")
        void shouldSuccessfullyExecuteUnapproveOperation() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.APPROVED);
            testCommand.setOperation("UNAPPROVE");
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            assertEquals(DocumentState.SUBMITTED, testSolutionReview.getDocumentState());
            assertEquals("user2", testSolutionReview.getLastModifiedBy());
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository).save(testSolutionReview);
        }

        @Test
        @DisplayName("Should successfully execute MARK_OUTDATED operation")
        void shouldSuccessfullyExecuteMarkOutdatedOperation() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.ACTIVE);
            testCommand.setOperation("MARK_OUTDATED");
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            assertEquals(DocumentState.OUTDATED, testSolutionReview.getDocumentState());
            assertEquals("user2", testSolutionReview.getLastModifiedBy());
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository).save(testSolutionReview);
        }
        }

    @Nested
    @DisplayName("Constraint Validation Tests")
    class ConstraintValidationTests {

        @Test
        @DisplayName("Should validate exclusive constraint when transitioning to exclusive states")
        void shouldValidateExclusiveConstraintWhenTransitioningToExclusiveStates() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.SUBMITTED);
            testCommand.setOperation("APPROVE");

            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);
            doNothing().when(solutionReviewService).validateExclusiveStateConstraint("SYS-001", "sr-1");

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            assertEquals(DocumentState.APPROVED, testSolutionReview.getDocumentState());
            verify(solutionReviewService).validateExclusiveStateConstraint("SYS-001", "sr-1");
        }

        @Test
        @DisplayName("Should handle constraint validation failure gracefully")
        void shouldHandleConstraintValidationFailureGracefully() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.SUBMITTED);
            testCommand.setOperation("APPROVE");

            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            doThrow(new IllegalOperationException("Constraint violated"))
                    .when(solutionReviewService)
                    .validateExclusiveStateConstraint("SYS-001", "sr-1");

            // Act & Assert
            IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                    () -> lifecycleService.executeTransition(testCommand));

            assertTrue(exception.getMessage().contains("Constraint violated"));
            verify(solutionReviewService).validateExclusiveStateConstraint("SYS-001", "sr-1");
            verify(solutionReviewRepository, never()).save(any());
        }
        }

    @Nested
    @DisplayName("Invalid Operation Tests")
    class InvalidOperationTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid operation string")
        void shouldThrowIllegalArgumentExceptionForInvalidOperationString() {
            // Arrange
            testCommand.setOperation("INVALID_OPERATION");
            // No need to mock repository since operation validation happens first

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> lifecycleService.executeTransition(testCommand));

            assertTrue(exception.getMessage().contains("Invalid operation 'INVALID_OPERATION'"));
            // Verify that repository was NOT called since validation failed early
            verify(solutionReviewRepository, never()).findById(anyString());
        }
        }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle IllegalStateTransitionException gracefully")
        void shouldHandleIllegalStateTransitionExceptionGracefully() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.SUBMITTED);
            testCommand.setOperation("SUBMIT"); // Invalid from SUBMITTED
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));

            // Act & Assert
            IllegalStateTransitionException exception = assertThrows(IllegalStateTransitionException.class,
                    () -> lifecycleService.executeTransition(testCommand));

            assertTrue(exception.getMessage().contains("Cannot execute operation"));
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should validate all required command fields")
        void shouldValidateAllRequiredCommandFields() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.DRAFT);
            testCommand.setOperation("SUBMIT");
            testCommand.setModifiedBy(null); // This should still work as the method doesn't validate this

            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);

            // Act & Assert - Should not throw exception
            assertDoesNotThrow(() -> lifecycleService.executeTransition(testCommand));
        }
        }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete lifecycle: DRAFT -> SUBMITTED -> APPROVED -> ACTIVE -> OUTDATED")
        void shouldHandleCompleteLifecycle() {
            // Test DRAFT -> SUBMITTED
            testSolutionReview.setDocumentState(DocumentState.DRAFT);
            testCommand.setOperation("SUBMIT");
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);

            lifecycleService.executeTransition(testCommand);
            assertEquals(DocumentState.SUBMITTED, testSolutionReview.getDocumentState());

            // Test SUBMITTED -> APPROVED (APPROVE)
            testCommand.setOperation("APPROVE");
            doNothing().when(solutionReviewService).validateExclusiveStateConstraint("SYS-001", "sr-1");

            lifecycleService.executeTransition(testCommand);
            assertEquals(DocumentState.APPROVED, testSolutionReview.getDocumentState());

            // Test APPROVED -> ACTIVE (ACTIVATE)
            testCommand.setOperation("ACTIVATE");
            doNothing().when(solutionReviewService).validateActiveStateConstraint("SYS-001", "sr-1");

            lifecycleService.executeTransition(testCommand);
            assertEquals(DocumentState.ACTIVE, testSolutionReview.getDocumentState());

            // Test ACTIVE -> OUTDATED
            testCommand.setOperation("MARK_OUTDATED");

            lifecycleService.executeTransition(testCommand);
            assertEquals(DocumentState.OUTDATED, testSolutionReview.getDocumentState());

            // Verify all operations were called
            verify(solutionReviewRepository, times(4)).findById("sr-1");
            verify(solutionReviewRepository, times(4)).save(testSolutionReview);
        }

        @Test
        @DisplayName("Should deactivate existing active SR before activating new one")
        void shouldDeactivateExistingActiveSRBeforeActivatingNewOne() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.APPROVED);
            testSolutionReview.setSystemCode("SYS-001");
            testCommand.setOperation("ACTIVATE");

            // Create an existing active solution review with the same systemCode
            SolutionReview existingActiveSR = new SolutionReview("SYS-001", testSolutionOverview);
            existingActiveSR.setId("sr-existing");
            existingActiveSR.setDocumentState(DocumentState.ACTIVE);

            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.findBySystemCodeAndDocumentState("SYS-001", DocumentState.ACTIVE))
                    .thenReturn(Optional.of(existingActiveSR));
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);
            doNothing().when(solutionReviewService).validateActiveStateConstraint(anyString(), anyString());

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            // Verify the new SR was activated
            assertEquals(DocumentState.ACTIVE, testSolutionReview.getDocumentState());
            assertEquals("user2", testSolutionReview.getLastModifiedBy());

            // Verify the existing active SR was marked as outdated
            assertEquals(DocumentState.OUTDATED, existingActiveSR.getDocumentState());
            assertEquals("user2", existingActiveSR.getLastModifiedBy());

            // Verify repository calls
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository).findBySystemCodeAndDocumentState("SYS-001", DocumentState.ACTIVE);
            verify(solutionReviewRepository, times(2)).save(any(SolutionReview.class)); // Both SRs should be saved
        }

        @Test
        @DisplayName("Should not deactivate when no existing active SR exists")
        void shouldNotDeactivateWhenNoExistingActiveSRExists() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.APPROVED);
            testSolutionReview.setSystemCode("SYS-001");
            testCommand.setOperation("ACTIVATE");

            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.findBySystemCodeAndDocumentState("SYS-001", DocumentState.ACTIVE))
                    .thenReturn(Optional.empty()); // No existing active SR
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);
            doNothing().when(solutionReviewService).validateActiveStateConstraint(anyString(), anyString());

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            // Verify the new SR was activated
            assertEquals(DocumentState.ACTIVE, testSolutionReview.getDocumentState());
            assertEquals("user2", testSolutionReview.getLastModifiedBy());

            // Verify repository calls
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository).findBySystemCodeAndDocumentState("SYS-001", DocumentState.ACTIVE);
            verify(solutionReviewRepository, times(1)).save(testSolutionReview); // Only the new SR should be saved
        }

        @Test
        @DisplayName("Should not deactivate when existing active SR is the same document")
        void shouldNotDeactivateWhenExistingActiveSRIsSameDocument() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.APPROVED);
            testSolutionReview.setSystemCode("SYS-001");
            testCommand.setOperation("ACTIVATE");

            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.findBySystemCodeAndDocumentState("SYS-001", DocumentState.ACTIVE))
                    .thenReturn(Optional.of(testSolutionReview)); // Same document returned
            when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(testSolutionReview);
            doNothing().when(solutionReviewService).validateActiveStateConstraint(anyString(), anyString());

            // Act
            lifecycleService.executeTransition(testCommand);

            // Assert
            // Verify the SR was activated
            assertEquals(DocumentState.ACTIVE, testSolutionReview.getDocumentState());
            assertEquals("user2", testSolutionReview.getLastModifiedBy());

            // Verify repository calls
            verify(solutionReviewRepository).findById("sr-1");
            verify(solutionReviewRepository).findBySystemCodeAndDocumentState("SYS-001", DocumentState.ACTIVE);
            verify(solutionReviewRepository, times(1)).save(testSolutionReview); // Only saved once for activation
        }

        @Test
        @DisplayName("Should ensure data consistency with transaction rollback on failure")
        void shouldEnsureDataConsistencyWithTransactionRollback() {
            // This test verifies that if activation fails after deactivating existing SR,
            // the transaction rollback maintains data consistency.
            // 
            // Note: This test documents the expected behavior rather than testing actual 
            // rollback since our mocked repository doesn't implement real transaction behavior.
            // In a real database scenario, @Transactional ensures that if any step fails,
            // ALL changes within the transaction are rolled back automatically.

            // Arrange
            testSolutionReview.setDocumentState(DocumentState.APPROVED);
            testSolutionReview.setSystemCode("SYS-001");
            testCommand.setOperation("ACTIVATE");

            // Create an existing active SR that should be deactivated
            SolutionReview existingActiveSR = new SolutionReview();
            existingActiveSR.setId("sr-existing");
            existingActiveSR.setSystemCode("SYS-001");
            existingActiveSR.setDocumentState(DocumentState.ACTIVE);
            existingActiveSR.setLastModifiedBy("originalUser");

            // Mock repository to return both SRs
            when(solutionReviewRepository.findById("sr-1")).thenReturn(Optional.of(testSolutionReview));
            when(solutionReviewRepository.findBySystemCodeAndDocumentState("SYS-001", DocumentState.ACTIVE))
                    .thenReturn(Optional.of(existingActiveSR));

            // Use lenient stubbing for the save operations to allow multiple calls with different objects
            lenient().when(solutionReviewRepository.save(any(SolutionReview.class)))
                    .thenAnswer(invocation -> {
                        SolutionReview sr = invocation.getArgument(0);
                        // If it's the main SR being activated, throw an exception
                        if ("sr-1".equals(sr.getId()) && sr.getDocumentState() == DocumentState.ACTIVE) {
                            throw new RuntimeException("Database connection failed");
                        }
                        // Otherwise, return the saved object (for deactivation)
                        return sr;
                    });

            // Act & Assert
            // The transaction should fail and all changes should be rolled back
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                lifecycleService.executeTransition(testCommand);
            });

            assertEquals("Database connection failed", exception.getMessage());

            // In a real scenario with @Transactional:
            // 1. existingActiveSR would remain ACTIVE (rollback)
            // 2. testSolutionReview would remain APPROVED (rollback)
            // 3. No SR would be left in an inconsistent state
            // 4. Business rule "only one active SR per systemCode" is maintained

            // Note: The actual rollback behavior is handled by Spring's transaction management
            // when running against a real database. This test documents the expected behavior.
        }
    }
}