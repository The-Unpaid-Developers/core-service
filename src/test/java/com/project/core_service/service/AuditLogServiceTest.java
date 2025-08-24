package com.project.core_service.service;

import com.project.core_service.models.audit.AuditLogMeta;
import com.project.core_service.models.audit.AuditLogNode;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.solution_overview.SolutionDetails;
import com.project.core_service.models.solution_overview.BusinessUnit;
import com.project.core_service.models.solution_overview.BusinessDriver;
import com.project.core_service.repositories.AuditLogMetaRepository;
import com.project.core_service.repositories.AuditLogNodeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService Tests")
class AuditLogServiceTest {

    @Mock
    private AuditLogMetaRepository auditLogMetaRepository;

    @Mock
    private AuditLogNodeRepository auditLogNodeRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLogMeta testAuditLogMeta;
    private AuditLogNode testAuditLogNode;
    private SolutionReview testSolutionReview;
    private SolutionOverview testSolutionOverview;

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
        testSolutionReview.setDocumentState(DocumentState.CURRENT);

        testAuditLogMeta = new AuditLogMeta("sr-1", "SYS-001");
        testAuditLogMeta.setHead("node-1");

        testAuditLogNode = new AuditLogNode("sr-1", "Test change", "v1.0.1");
        testAuditLogNode.setId("node-1");
    }

    @Nested
    @DisplayName("AuditLogMeta Operations")
    class AuditLogMetaOperationsTests {

        @Test
        @DisplayName("Should create audit log meta successfully")
        void shouldCreateAuditLogMetaSuccessfully() {
            // Arrange
            when(auditLogMetaRepository.save(testAuditLogMeta)).thenReturn(testAuditLogMeta);

            // Act
            auditLogService.createAuditLogMeta(testAuditLogMeta);

            // Assert
            verify(auditLogMetaRepository).save(testAuditLogMeta);
        }

        @Test
        @DisplayName("Should get audit log meta successfully when exists")
        void shouldGetAuditLogMetaSuccessfullyWhenExists() {
            // Arrange
            when(auditLogMetaRepository.findById("SYS-001")).thenReturn(Optional.of(testAuditLogMeta));

            // Act
            AuditLogMeta result = auditLogService.getAuditLogMeta("SYS-001");

            // Assert
            assertNotNull(result);
            assertEquals(testAuditLogMeta, result);
            assertEquals("SYS-001", result.getSystemCode());
            assertEquals("node-1", result.getHead());
            verify(auditLogMetaRepository).findById("SYS-001");
        }

        @Test
        @DisplayName("Should return null when audit log meta does not exist")
        void shouldReturnNullWhenAuditLogMetaDoesNotExist() {
            // Arrange
            when(auditLogMetaRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());

            // Act
            AuditLogMeta result = auditLogService.getAuditLogMeta("NON-EXISTENT");

            // Assert
            assertNull(result);
            verify(auditLogMetaRepository).findById("NON-EXISTENT");
        }

        @Test
        @DisplayName("Should update audit log meta successfully")
        void shouldUpdateAuditLogMetaSuccessfully() {
            // Arrange
            testAuditLogMeta.setHead("new-head-node");
            when(auditLogMetaRepository.save(testAuditLogMeta)).thenReturn(testAuditLogMeta);

            // Act
            auditLogService.updateAuditLogMeta(testAuditLogMeta);

            // Assert
            verify(auditLogMetaRepository).save(testAuditLogMeta);
        }
    }

    @Nested
    @DisplayName("AuditLogNode Operations")
    class AuditLogNodeOperationsTests {

        @Test
        @DisplayName("Should get audit log node successfully when exists")
        void shouldGetAuditLogNodeSuccessfullyWhenExists() {
            // Arrange
            when(auditLogNodeRepository.findById("node-1")).thenReturn(Optional.of(testAuditLogNode));

            // Act
            AuditLogNode result = auditLogService.getAuditLogNode("node-1");

            // Assert
            assertNotNull(result);
            assertEquals(testAuditLogNode, result);
            assertEquals("node-1", result.getId());
            assertEquals("sr-1", result.getSolutionReviewId());
            assertEquals("Test change", result.getChangeDescription());
            verify(auditLogNodeRepository).findById("node-1");
        }

        @Test
        @DisplayName("Should return null when audit log node does not exist")
        void shouldReturnNullWhenAuditLogNodeDoesNotExist() {
            // Arrange
            when(auditLogNodeRepository.findById("NON-EXISTENT")).thenReturn(Optional.empty());

            // Act
            AuditLogNode result = auditLogService.getAuditLogNode("NON-EXISTENT");

            // Assert
            assertNull(result);
            verify(auditLogNodeRepository).findById("NON-EXISTENT");
        }

        @Test
        @DisplayName("Should update audit log node successfully")
        void shouldUpdateAuditLogNodeSuccessfully() {
            // Arrange
            LocalDateTime beforeUpdate = testAuditLogNode.getTimestamp();
            String newChangeDescription = "Updated change description";
            when(auditLogNodeRepository.save(testAuditLogNode)).thenReturn(testAuditLogNode);

            // Act
            auditLogService.updateAuditLogNode(testAuditLogNode, newChangeDescription);

            // Assert
            assertEquals(newChangeDescription, testAuditLogNode.getChangeDescription());
            assertTrue(testAuditLogNode.getTimestamp().isAfter(beforeUpdate) ||
                    testAuditLogNode.getTimestamp().equals(beforeUpdate));
            verify(auditLogNodeRepository).save(testAuditLogNode);
        }
    }

    @Nested
    @DisplayName("Add Solution Review to Audit Log")
    class AddSolutionReviewToAuditLogTests {

        @Test
        @DisplayName("Should add CURRENT solution review to audit log successfully")
        void shouldAddCurrentSolutionReviewToAuditLogSuccessfully() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.CURRENT);
            String changeDescription = "Approved and set as current";
            String srVersion = "v1.0.1";

            when(auditLogNodeRepository.save(any(AuditLogNode.class))).thenAnswer(invocation -> {
                AuditLogNode node = invocation.getArgument(0);
                node.setId("generated-node-id");
                return node;
            });
            when(auditLogMetaRepository.save(testAuditLogMeta)).thenReturn(testAuditLogMeta);

            // Act
            auditLogService.addSolutionReviewToAuditLog(testAuditLogMeta, testSolutionReview,
                    changeDescription, srVersion);

            // Assert
            verify(auditLogNodeRepository).save(argThat(node -> node.getSolutionReviewId().equals("sr-1") &&
                    node.getChangeDescription().equals(changeDescription) &&
                    node.getSolutionsReviewVersion().equals(srVersion)));
            verify(auditLogMetaRepository).save(testAuditLogMeta);
        }

        @Test
        @DisplayName("Should add OUTDATED solution review to audit log successfully")
        void shouldAddOutdatedSolutionReviewToAuditLogSuccessfully() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.OUTDATED);
            String changeDescription = "Marked as outdated";
            String srVersion = "v1.0.0";

            when(auditLogNodeRepository.save(any(AuditLogNode.class))).thenAnswer(invocation -> {
                AuditLogNode node = invocation.getArgument(0);
                node.setId("generated-node-id");
                return node;
            });
            when(auditLogMetaRepository.save(testAuditLogMeta)).thenReturn(testAuditLogMeta);

            // Act
            auditLogService.addSolutionReviewToAuditLog(testAuditLogMeta, testSolutionReview,
                    changeDescription, srVersion);

            // Assert
            verify(auditLogNodeRepository).save(argThat(node -> node.getSolutionReviewId().equals("sr-1") &&
                    node.getChangeDescription().equals(changeDescription) &&
                    node.getSolutionsReviewVersion().equals(srVersion)));
            verify(auditLogMetaRepository).save(testAuditLogMeta);
        }

        @Test
        @DisplayName("Should not add DRAFT solution review to audit log")
        void shouldNotAddDraftSolutionReviewToAuditLog() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.DRAFT);
            String changeDescription = "Should not be added";
            String srVersion = "v1.0.1";

            // Act
            auditLogService.addSolutionReviewToAuditLog(testAuditLogMeta, testSolutionReview,
                    changeDescription, srVersion);

            // Assert
            verify(auditLogNodeRepository, never()).save(any(AuditLogNode.class));
            verify(auditLogMetaRepository, never()).save(any(AuditLogMeta.class));
        }

        @Test
        @DisplayName("Should not add SUBMITTED solution review to audit log")
        void shouldNotAddSubmittedSolutionReviewToAuditLog() {
            // Arrange
            testSolutionReview.setDocumentState(DocumentState.SUBMITTED);
            String changeDescription = "Should not be added";
            String srVersion = "v1.0.1";

            // Act
            auditLogService.addSolutionReviewToAuditLog(testAuditLogMeta, testSolutionReview,
                    changeDescription, srVersion);

            // Assert
            verify(auditLogNodeRepository, never()).save(any(AuditLogNode.class));
            verify(auditLogMetaRepository, never()).save(any(AuditLogMeta.class));
        }

        @Test
        @DisplayName("Should handle empty string parameters gracefully")
        void shouldHandleEmptyStringParametersGracefully() {
            // Test with empty changeDescription
            testSolutionReview.setDocumentState(DocumentState.CURRENT);
            when(auditLogNodeRepository.save(any(AuditLogNode.class))).thenAnswer(invocation -> {
                AuditLogNode node = invocation.getArgument(0);
                node.setId("generated-node-id");
                return node;
            });
            when(auditLogMetaRepository.save(testAuditLogMeta)).thenReturn(testAuditLogMeta);

            assertDoesNotThrow(() -> auditLogService.addSolutionReviewToAuditLog(
                    testAuditLogMeta, testSolutionReview, "", "v1.0.1"));

            // Test with empty srVersion
            assertDoesNotThrow(() -> auditLogService.addSolutionReviewToAuditLog(
                    testAuditLogMeta, testSolutionReview, "description", ""));

            // Verify that nodes were created despite empty strings
            verify(auditLogNodeRepository, times(2)).save(any(AuditLogNode.class));
            verify(auditLogMetaRepository, times(2)).save(testAuditLogMeta);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete audit log workflow")
        void shouldHandleCompleteAuditLogWorkflow() {
            // Step 1: Create audit log meta
            when(auditLogMetaRepository.save(any(AuditLogMeta.class))).thenReturn(testAuditLogMeta);
            auditLogService.createAuditLogMeta(testAuditLogMeta);

            // Step 2: Add solution review to audit log
            testSolutionReview.setDocumentState(DocumentState.CURRENT);
            when(auditLogNodeRepository.save(any(AuditLogNode.class))).thenAnswer(invocation -> {
                AuditLogNode node = invocation.getArgument(0);
                node.setId("node-1");
                return node;
            });

            auditLogService.addSolutionReviewToAuditLog(testAuditLogMeta, testSolutionReview,
                    "Initial approval", "v1.0.1");

            // Step 3: Get audit log meta
            when(auditLogMetaRepository.findById("SYS-001")).thenReturn(Optional.of(testAuditLogMeta));
            AuditLogMeta retrievedMeta = auditLogService.getAuditLogMeta("SYS-001");

            // Step 4: Get audit log node
            when(auditLogNodeRepository.findById("node-1")).thenReturn(Optional.of(testAuditLogNode));
            AuditLogNode retrievedNode = auditLogService.getAuditLogNode("node-1");

            // Step 5: Update audit log node
            auditLogService.updateAuditLogNode(retrievedNode, "Updated description");

            // Verify all interactions
            verify(auditLogMetaRepository, times(2)).save(any(AuditLogMeta.class));
            verify(auditLogNodeRepository, times(2)).save(any(AuditLogNode.class));
            verify(auditLogMetaRepository).findById("SYS-001");
            verify(auditLogNodeRepository).findById("node-1");
            verify(auditLogNodeRepository, times(2)).save(any(AuditLogNode.class));

            assertNotNull(retrievedMeta);
            assertNotNull(retrievedNode);
            assertEquals("Updated description", retrievedNode.getChangeDescription());
        }

        @Test
        @DisplayName("Should handle multiple solution reviews in audit log")
        void shouldHandleMultipleSolutionReviewsInAuditLog() {
            // Create multiple solution reviews
            SolutionReview sr1 = new SolutionReview("SYS-001", testSolutionOverview);
            sr1.setId("sr-1");
            sr1.setDocumentState(DocumentState.CURRENT);

            SolutionReview sr2 = new SolutionReview("SYS-001", testSolutionOverview);
            sr2.setId("sr-2");
            sr2.setDocumentState(DocumentState.OUTDATED);

            when(auditLogNodeRepository.save(any(AuditLogNode.class))).thenAnswer(invocation -> {
                AuditLogNode node = invocation.getArgument(0);
                node.setId("node-" + System.currentTimeMillis());
                return node;
            });
            when(auditLogMetaRepository.save(testAuditLogMeta)).thenReturn(testAuditLogMeta);

            // Add both solution reviews
            auditLogService.addSolutionReviewToAuditLog(testAuditLogMeta, sr1, "First approval", "v1.0.1");
            auditLogService.addSolutionReviewToAuditLog(testAuditLogMeta, sr2, "Second approval", "v1.0.2");

            // Verify both were added
            verify(auditLogNodeRepository, times(2)).save(any(AuditLogNode.class));
            verify(auditLogMetaRepository, times(2)).save(testAuditLogMeta);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Test exception during save
            when(auditLogMetaRepository.save(any(AuditLogMeta.class)))
                    .thenThrow(new RuntimeException("Database error"));

            assertThrows(RuntimeException.class, () -> auditLogService.createAuditLogMeta(testAuditLogMeta));
        }

        @Test
        @DisplayName("Should handle empty audit log operations")
        void shouldHandleEmptyAuditLogOperations() {
            // Test getting non-existent audit log meta
            when(auditLogMetaRepository.findById("EMPTY")).thenReturn(Optional.empty());
            AuditLogMeta result = auditLogService.getAuditLogMeta("EMPTY");
            assertNull(result);

            // Test getting non-existent audit log node
            when(auditLogNodeRepository.findById("EMPTY")).thenReturn(Optional.empty());
            AuditLogNode nodeResult = auditLogService.getAuditLogNode("EMPTY");
            assertNull(nodeResult);
        }
    }
}
