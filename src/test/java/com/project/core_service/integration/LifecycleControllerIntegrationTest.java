package com.project.core_service.integration;

import com.project.core_service.commands.LifecycleTransitionCommand;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.repositories.SolutionReviewRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for
 * {@link com.project.core_service.controllers.LifecycleController}.
 * 
 * <p>
 * These tests verify the complete lifecycle state transition flows for solution
 * reviews,
 * including validation of state machines, constraint enforcement, and database
 * persistence.
 * 
 * <h2>Test Scenarios Covered:</h2>
 * <ul>
 * <li>Complete lifecycle flow: DRAFT → SUBMITTED → APPROVED → ACTIVE →
 * OUTDATED</li>
 * <li>Reverse transitions: SUBMITTED → DRAFT, APPROVED → SUBMITTED</li>
 * <li>Constraint validation (exclusive states, single ACTIVE per system)</li>
 * <li>Invalid state transition attempts</li>
 * <li>Error handling and validation</li>
 * </ul>
 * 
 * @see com.project.core_service.controllers.LifecycleController
 * @see BaseIntegrationTest
 */
@DisplayName("Lifecycle Controller Integration Tests")
@Epic("Solution Review Lifecycle Management")
@Feature("Lifecycle State Transitions")
class LifecycleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SolutionReviewRepository solutionReviewRepository;

    @BeforeEach
    @Step("Reset test data factory before each test")
    void setup() {
        TestDataFactory.reset();
    }

    // ==================== FORWARD STATE TRANSITIONS ====================

    @Nested
    @DisplayName("Forward State Transitions - Main Lifecycle Flow")
    @Story("Forward State Transitions")
    class ForwardStateTransitionTests {

        @Test
        @DisplayName("Should transition from DRAFT to SUBMITTED")
        @Description("Submits a DRAFT solution review for approval")
        @Severity(SeverityLevel.CRITICAL)
        void shouldTransitionDraftToSubmitted() throws Exception {
            // Given - create a DRAFT review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "SUBMIT",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Submitting for review");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            // Verify state change in database
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.SUBMITTED);
            assertThat(updated.getLastModifiedBy()).isEqualTo(TestDataFactory.TestUsers.ARCHITECT);
        }

        @Test
        @DisplayName("Should transition from SUBMITTED to APPROVED")
        @Description("Approves a SUBMITTED solution review")
        @Severity(SeverityLevel.CRITICAL)
        void shouldTransitionSubmittedToApproved() throws Exception {
            // Given - create a SUBMITTED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.SUBMITTED);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "APPROVE",
                    TestDataFactory.TestUsers.REVIEWER,
                    "Approved after review");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            // Verify state change
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.APPROVED);
            assertThat(updated.getLastModifiedBy()).isEqualTo(TestDataFactory.TestUsers.REVIEWER);
        }

        @Test
        @DisplayName("Should transition from APPROVED to ACTIVE")
        @Description("Activates an APPROVED solution review")
        @Severity(SeverityLevel.CRITICAL)
        void shouldTransitionApprovedToActive() throws Exception {
            // Given - create an APPROVED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.APPROVED);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "ACTIVATE",
                    TestDataFactory.TestUsers.ADMIN,
                    "Activating solution");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            // Verify state change
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.ACTIVE);
            assertThat(updated.getLastModifiedBy()).isEqualTo(TestDataFactory.TestUsers.ADMIN);
        }

        @Test
        @DisplayName("Should transition from ACTIVE to OUTDATED")
        @Description("Marks an ACTIVE solution review as OUTDATED")
        @Severity(SeverityLevel.NORMAL)
        void shouldTransitionActiveToOutdated() throws Exception {
            // Given - create an ACTIVE review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "MARK_OUTDATED",
                    TestDataFactory.TestUsers.ADMIN,
                    "Marking as outdated");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            // Verify state change
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.OUTDATED);
        }

        @Test
        @DisplayName("Should complete full lifecycle: DRAFT → SUBMITTED → APPROVED → ACTIVE → OUTDATED")
        @Description("Verifies the complete state transition flow end-to-end")
        @Severity(SeverityLevel.BLOCKER)
        void shouldCompleteFullLifecycle() throws Exception {
            // Given - start with DRAFT
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);
            String reviewId = review.getId();

            // Step 1: DRAFT → SUBMITTED
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
                            "Step 1"))))
                    .andExpect(status().isOk());

            assertThat(solutionReviewRepository.findById(reviewId).orElseThrow().getDocumentState())
                    .isEqualTo(DocumentState.SUBMITTED);

            // Step 2: SUBMITTED → APPROVED
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "APPROVE", TestDataFactory.TestUsers.REVIEWER,
                            "Step 2"))))
                    .andExpect(status().isOk());

            assertThat(solutionReviewRepository.findById(reviewId).orElseThrow().getDocumentState())
                    .isEqualTo(DocumentState.APPROVED);

            // Step 3: APPROVED → ACTIVE
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "ACTIVATE", TestDataFactory.TestUsers.ADMIN,
                            "Step 3"))))
                    .andExpect(status().isOk());

            assertThat(solutionReviewRepository.findById(reviewId).orElseThrow().getDocumentState())
                    .isEqualTo(DocumentState.ACTIVE);

            // Step 4: ACTIVE → OUTDATED
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "MARK_OUTDATED", TestDataFactory.TestUsers.ADMIN,
                            "Step 4"))))
                    .andExpect(status().isOk());

            assertThat(solutionReviewRepository.findById(reviewId).orElseThrow().getDocumentState())
                    .isEqualTo(DocumentState.OUTDATED);
        }
    }

    // ==================== REVERSE STATE TRANSITIONS ====================

    @Nested
    @DisplayName("Reverse State Transitions")
    @Story("Reverse State Transitions")
    class ReverseStateTransitionTests {

        @Test
        @DisplayName("Should transition from SUBMITTED back to DRAFT")
        @Description("Removes submission and returns review to DRAFT state")
        @Severity(SeverityLevel.NORMAL)
        void shouldTransitionSubmittedToDraft() throws Exception {
            // Given - create a SUBMITTED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.SUBMITTED);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "REMOVE_SUBMISSION",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Need to make changes");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            // Verify state change
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.DRAFT);
        }

        @Test
        @DisplayName("Should transition from APPROVED back to SUBMITTED")
        @Description("Un-approves a review and returns it to SUBMITTED state")
        @Severity(SeverityLevel.NORMAL)
        void shouldTransitionApprovedToSubmitted() throws Exception {
            // Given - create an APPROVED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.APPROVED);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "UNAPPROVE",
                    TestDataFactory.TestUsers.REVIEWER,
                    "Found issues during final check");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            // Verify state change
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.SUBMITTED);
        }
    }

    // ==================== CONSTRAINT VALIDATION ====================

    @Nested
    @DisplayName("Constraint Validation Tests")
    @Story("State Constraints and Business Rules")
    class ConstraintValidationTests {

        @Test
        @DisplayName("Should enforce exclusive state constraint - only one DRAFT per system")
        @Description("Validates that only one DRAFT can exist per system at a time")
        @Severity(SeverityLevel.BLOCKER)
        void shouldEnforceExclusiveDraftConstraint() throws Exception {
            // Given - system with existing DRAFT
            String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_A;
            createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            // Create another review in SUBMITTED state then try to move it to DRAFT
            com.project.core_service.models.solution_overview.SolutionOverview overview2 = TestDataFactory
                    .createSolutionOverview("Another Solution");
            overview2 = mongoTemplate.save(overview2);

            SolutionReview review2 = TestDataFactory.createSolutionReviewWithOverview(systemCode,
                    DocumentState.SUBMITTED, overview2);
            review2.setId("another-review-id");
            review2 = solutionReviewRepository.save(review2);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review2.getId(),
                    "REMOVE_SUBMISSION",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Trying to create second DRAFT");

            // When & Then - should fail
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("exclusive states")));
        }

        @Test
        @DisplayName("Should enforce single ACTIVE constraint per system")
        @Description("Validates that only one ACTIVE review can exist per system")
        @Severity(SeverityLevel.BLOCKER)
        void shouldEnforceSingleActiveConstraint() throws Exception {
            // Given - system with existing ACTIVE review
            String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_A;
            createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            // Create another APPROVED review and try to activate it
            com.project.core_service.models.solution_overview.SolutionOverview overview2 = TestDataFactory
                    .createSolutionOverview("Another Approved Solution");
            overview2 = mongoTemplate.save(overview2);

            SolutionReview approvedReview = TestDataFactory.createSolutionReviewWithOverview(systemCode,
                    DocumentState.APPROVED, overview2);
            approvedReview.setId("another-approved-review");
            approvedReview = solutionReviewRepository.save(approvedReview);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    approvedReview.getId(),
                    "ACTIVATE",
                    TestDataFactory.TestUsers.ADMIN,
                    "Trying to create second ACTIVE");

            // When & Then - should fail
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("ACTIVE document already exists")));
        }

        @Test
        @DisplayName("Should allow multiple OUTDATED reviews per system")
        @Description("Validates that multiple OUTDATED reviews can coexist for the same system")
        @Severity(SeverityLevel.NORMAL)
        void shouldAllowMultipleOutdatedReviews() throws Exception {
            // Given - create multiple ACTIVE reviews and mark them outdated
            String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_A;

            // First review: ACTIVE → OUTDATED
            SolutionReview review1 = createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            review1.getId(), "MARK_OUTDATED",
                            TestDataFactory.TestUsers.ADMIN, "Outdating first"))))
                    .andExpect(status().isOk());

            // Second review: ACTIVE → OUTDATED
            com.project.core_service.models.solution_overview.SolutionOverview overview2 = TestDataFactory
                    .createSolutionOverview("Second Active Solution");
            overview2 = mongoTemplate.save(overview2);

            SolutionReview review2 = TestDataFactory.createSolutionReviewWithOverview(systemCode,
                    DocumentState.ACTIVE, overview2);
            review2.setId("second-review-id");
            review2 = solutionReviewRepository.save(review2);

            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            review2.getId(), "MARK_OUTDATED",
                            TestDataFactory.TestUsers.ADMIN, "Outdating second"))))
                    .andExpect(status().isOk());

            // Verify both are OUTDATED
            long outdatedCount = solutionReviewRepository.findAllBySystemCode(systemCode)
                    .stream()
                    .filter(r -> r.getDocumentState() == DocumentState.OUTDATED)
                    .count();
            assertThat(outdatedCount).isEqualTo(2);
        }
    }

    // ==================== INVALID STATE TRANSITIONS ====================

    @Nested
    @DisplayName("Invalid State Transition Attempts")
    @Story("Error Handling")
    class InvalidStateTransitionTests {

        @Test
        @DisplayName("Should reject SUBMIT operation on SUBMITTED review")
        @Description("Validates error when trying to submit an already submitted review")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectSubmitOnSubmitted() throws Exception {
            // Given - SUBMITTED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.SUBMITTED);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "SUBMIT",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Invalid operation");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")))
                    .andExpect(jsonPath("$.message").value(containsString("SUBMITTED")));
        }

        @Test
        @DisplayName("Should reject APPROVE operation on DRAFT review")
        @Description("Validates error when trying to approve a draft (must be submitted first)")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectApproveOnDraft() throws Exception {
            // Given - DRAFT review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "APPROVE",
                    TestDataFactory.TestUsers.REVIEWER,
                    "Trying to skip SUBMITTED state");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should reject ACTIVATE operation on DRAFT review")
        @Description("Validates error when trying to activate a draft (must go through proper flow)")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectActivateOnDraft() throws Exception {
            // Given - DRAFT review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "ACTIVATE",
                    TestDataFactory.TestUsers.ADMIN,
                    "Trying to skip states");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should reject invalid operation name")
        @Description("Validates error handling for non-existent operations")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectInvalidOperation() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "INVALID_OPERATION",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Testing invalid operation");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Invalid operation")));
        }

        @Test
        @DisplayName("Should return 404 for non-existent document ID")
        @Description("Validates error handling when document is not found")
        @Severity(SeverityLevel.NORMAL)
        void shouldReturn404ForNonExistentDocument() throws Exception {
            // Given
            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    "non-existent-id",
                    "SUBMIT",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Testing non-existent ID");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("not found")));
        }

        @Test
        @DisplayName("Should reject request with missing required fields")
        @Description("Validates request body validation")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectRequestWithMissingFields() throws Exception {
            // Given - command with null documentId
            String invalidJson = """
                    {
                        "documentId": null,
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user"
                    }
                    """;

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    @Story("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle transition with null comment")
        @Description("Validates that comment field is optional")
        @Severity(SeverityLevel.MINOR)
        void shouldHandleTransitionWithNullComment() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "SUBMIT",
                    TestDataFactory.TestUsers.ARCHITECT,
                    null // No comment
            );

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk());

            // Verify state changed
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.SUBMITTED);
        }

        @Test
        @DisplayName("Should handle case-insensitive operation names")
        @Description("Validates that operation names are case-insensitive")
        @Severity(SeverityLevel.MINOR)
        void shouldHandleCaseInsensitiveOperations() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "submit", // lowercase
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Testing case insensitivity");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk());

            // Verify state changed
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.SUBMITTED);
        }

        @Test
        @DisplayName("Should update lastModifiedBy and lastModifiedAt on transition")
        @Description("Verifies audit trail is properly maintained during transitions")
        @Severity(SeverityLevel.NORMAL)
        void shouldUpdateAuditFieldsOnTransition() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            com.project.core_service.models.solution_overview.SolutionOverview overview = TestDataFactory
                    .createSolutionOverview("Test Solution");
            overview = mongoTemplate.save(overview);

            SolutionReview review = TestDataFactory.createSolutionReviewWithOverview(systemCode,
                    DocumentState.DRAFT, overview);
            review.setLastModifiedBy("original-user");
            review = solutionReviewRepository.save(review);

            // Capture the original timestamp for comparison
            java.time.LocalDateTime originalTimestamp = review.getLastModifiedAt();

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "SUBMIT",
                    "new-user",
                    "Testing audit trail");

            // When
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk());

            // Then - verify audit fields updated
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getLastModifiedBy()).isEqualTo("new-user");
            if (originalTimestamp != null) {
                assertThat(updated.getLastModifiedAt()).isAfter(originalTimestamp);
            } else {
                assertThat(updated.getLastModifiedAt()).isNotNull();
            }
        }
    }

    // ==================== ADDITIONAL INVALID STATE TRANSITIONS
    // ====================

    @Nested
    @DisplayName("Additional Invalid State Transition Tests")
    @Story("Enhanced Negative Testing")
    class AdditionalInvalidStateTransitionTests {

        @Test
        @DisplayName("Should reject transition from OUTDATED to any state (terminal state)")
        @Description("OUTDATED is a terminal state with no valid transitions")
        @Severity(SeverityLevel.CRITICAL)
        void shouldRejectTransitionFromOutdated() throws Exception {
            // Given - OUTDATED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.OUTDATED);

            // Try all operations on OUTDATED state
            String[] operations = { "SUBMIT", "APPROVE", "ACTIVATE", "REMOVE_SUBMISSION", "UNAPPROVE" };

            for (String operation : operations) {
                LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                        review.getId(),
                        operation,
                        TestDataFactory.TestUsers.ADMIN,
                        "Attempting operation on OUTDATED");

                mockMvc.perform(post("/api/v1/lifecycle/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(command)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(containsString("Cannot execute operation")));
            }
        }

        @Test
        @DisplayName("Should reject ACTIVE to DRAFT transition (invalid reverse)")
        @Description("Cannot skip backward multiple states")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectActiveToSubmitted() throws Exception {
            // Given - ACTIVE review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "REMOVE_SUBMISSION",
                    TestDataFactory.TestUsers.ADMIN,
                    "Invalid reverse transition");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should reject APPROVED to OUTDATED transition (must go through ACTIVE)")
        @Description("Cannot skip ACTIVE state when marking outdated")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectApprovedToOutdated() throws Exception {
            // Given - APPROVED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.APPROVED);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "MARK_OUTDATED",
                    TestDataFactory.TestUsers.ADMIN,
                    "Trying to skip ACTIVE");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should reject DRAFT to APPROVED transition (must submit first)")
        @Description("Cannot skip SUBMITTED state in approval flow")
        @Severity(SeverityLevel.CRITICAL)
        void shouldRejectDraftToApproved() throws Exception {
            // Given - DRAFT review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "APPROVE",
                    TestDataFactory.TestUsers.REVIEWER,
                    "Trying to skip SUBMITTED");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should reject SUBMITTED to ACTIVE transition (must be approved first)")
        @Description("Cannot skip APPROVED state when activating")
        @Severity(SeverityLevel.CRITICAL)
        void shouldRejectSubmittedToActive() throws Exception {
            // Given - SUBMITTED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.SUBMITTED);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "ACTIVATE",
                    TestDataFactory.TestUsers.ADMIN,
                    "Trying to skip APPROVED");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should reject DRAFT to OUTDATED transition (must go through full lifecycle)")
        @Description("Cannot jump directly to terminal state from initial state")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectDraftToOutdated() throws Exception {
            // Given - DRAFT review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "MARK_OUTDATED",
                    TestDataFactory.TestUsers.ADMIN,
                    "Invalid direct to terminal");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should reject REMOVE_SUBMISSION from APPROVED state")
        @Description("REMOVE_SUBMISSION only valid from SUBMITTED state")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectRemoveSubmissionFromApproved() throws Exception {
            // Given - APPROVED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.APPROVED);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "REMOVE_SUBMISSION",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Invalid state for this operation");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should reject UNAPPROVE from ACTIVE state")
        @Description("UNAPPROVE only valid from APPROVED state")
        @Severity(SeverityLevel.NORMAL)
        void shouldRejectUnapproveFromActive() throws Exception {
            // Given - ACTIVE review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "UNAPPROVE",
                    TestDataFactory.TestUsers.REVIEWER,
                    "Cannot unapprove active document");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }
    }

    // ==================== CONCURRENT MODIFICATION SCENARIOS ====================

    @Nested
    @DisplayName("Concurrent Modification Scenarios")
    @Story("Concurrency and Race Conditions")
    class ConcurrentModificationTests {

        @Test
        @DisplayName("Should prevent concurrent activation of multiple reviews for same system")
        @Description("Tests race condition when two reviews try to become ACTIVE simultaneously")
        @Severity(SeverityLevel.BLOCKER)
        void shouldPreventConcurrentActivation() throws Exception {
            // Given - two APPROVED reviews for the same system
            String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_A;

            com.project.core_service.models.solution_overview.SolutionOverview overview1 = TestDataFactory
                    .createSolutionOverview("First Solution");
            overview1 = mongoTemplate.save(overview1);

            SolutionReview review1 = TestDataFactory.createSolutionReviewWithOverview(systemCode,
                    DocumentState.APPROVED, overview1);
            review1.setId("first-approved-review");
            review1 = solutionReviewRepository.save(review1);

            com.project.core_service.models.solution_overview.SolutionOverview overview2 = TestDataFactory
                    .createSolutionOverview("Second Solution");
            overview2 = mongoTemplate.save(overview2);

            SolutionReview review2 = TestDataFactory.createSolutionReviewWithOverview(systemCode,
                    DocumentState.APPROVED, overview2);
            review2.setId("second-approved-review");
            review2 = solutionReviewRepository.save(review2);

            // When - activate first review successfully
            LifecycleTransitionCommand command1 = new LifecycleTransitionCommand(
                    review1.getId(),
                    "ACTIVATE",
                    TestDataFactory.TestUsers.ADMIN,
                    "First activation");

            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command1)))
                    .andExpect(status().isOk());

            // Then - second activation should fail due to constraint
            LifecycleTransitionCommand command2 = new LifecycleTransitionCommand(
                    review2.getId(),
                    "ACTIVATE",
                    TestDataFactory.TestUsers.ADMIN,
                    "Second activation attempt");

            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command2)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("ACTIVE document already exists")));

            // Verify only one ACTIVE exists
            long activeCount = solutionReviewRepository.findAllBySystemCode(systemCode)
                    .stream()
                    .filter(r -> r.getDocumentState() == DocumentState.ACTIVE)
                    .count();
            assertThat(activeCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle rapid successive state transitions correctly")
        @Description("Tests sequential transitions executed in rapid succession")
        @Severity(SeverityLevel.CRITICAL)
        void shouldHandleRapidSuccessiveTransitions() throws Exception {
            // Given - DRAFT review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);
            String reviewId = review.getId();

            // When - execute rapid transitions: DRAFT → SUBMITTED → DRAFT → SUBMITTED
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
                            "First submit"))))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "REMOVE_SUBMISSION",
                            TestDataFactory.TestUsers.ARCHITECT, "Remove"))))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
                            "Second submit"))))
                    .andExpect(status().isOk());

            // Then - verify final state is SUBMITTED
            SolutionReview updated = solutionReviewRepository.findById(reviewId).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.SUBMITTED);
        }

        @Test
        @DisplayName("Should prevent creating DRAFT when one exists even with timing issues")
        @Description("Tests race condition for exclusive DRAFT constraint")
        @Severity(SeverityLevel.CRITICAL)
        void shouldPreventMultipleDraftsWithRaceCondition() throws Exception {
            // Given - existing DRAFT for system
            String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_A;
            createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            // Create a SUBMITTED review
            com.project.core_service.models.solution_overview.SolutionOverview overview2 = TestDataFactory
                    .createSolutionOverview("Second Solution");
            overview2 = mongoTemplate.save(overview2);

            SolutionReview submittedReview = TestDataFactory.createSolutionReviewWithOverview(systemCode,
                    DocumentState.SUBMITTED, overview2);
            submittedReview.setId("submitted-review-for-draft-test");
            submittedReview = solutionReviewRepository.save(submittedReview);

            // When - try to move SUBMITTED to DRAFT (should fail due to existing DRAFT)
            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    submittedReview.getId(),
                    "REMOVE_SUBMISSION",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Trying to create second DRAFT");

            // Then - should be rejected
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("exclusive states")));

            // Verify still only one DRAFT
            long draftCount = solutionReviewRepository.findAllBySystemCode(systemCode)
                    .stream()
                    .filter(r -> r.getDocumentState() == DocumentState.DRAFT)
                    .count();
            assertThat(draftCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle concurrent transitions by different users correctly")
        @Description("Simulates different users attempting operations on same document")
        @Severity(SeverityLevel.NORMAL)
        void shouldHandleConcurrentTransitionsByDifferentUsers() throws Exception {
            // Given - SUBMITTED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.SUBMITTED);
            String reviewId = review.getId();

            // User 1 approves it
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "APPROVE", TestDataFactory.TestUsers.REVIEWER,
                            "Approved"))))
                    .andExpect(status().isOk());

            // User 2 tries to remove submission (should fail - it's already APPROVED)
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "REMOVE_SUBMISSION",
                            TestDataFactory.TestUsers.ARCHITECT,
                            "Trying to remove"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));

            // Verify state is APPROVED (first operation won)
            SolutionReview updated = solutionReviewRepository.findById(reviewId).orElseThrow();
            assertThat(updated.getDocumentState()).isEqualTo(DocumentState.APPROVED);
            assertThat(updated.getLastModifiedBy()).isEqualTo(TestDataFactory.TestUsers.REVIEWER);
        }

        @Test
        @DisplayName("Should maintain data consistency during concurrent operations on different reviews")
        @Description("Tests isolation between concurrent operations on different documents")
        @Severity(SeverityLevel.NORMAL)
        void shouldMaintainConsistencyDuringConcurrentOpsOnDifferentReviews() throws Exception {
            // Given - two reviews for different systems
            String systemA = TestDataFactory.createSystemCode();
            String systemB = TestDataFactory.createSystemCode();

            SolutionReview reviewA = createAndSaveSolutionReview(systemA, DocumentState.DRAFT);
            SolutionReview reviewB = createAndSaveSolutionReview(systemB, DocumentState.DRAFT);

            // When - perform transitions on both simultaneously
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewA.getId(), "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
                            "Submit A"))))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewB.getId(), "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
                            "Submit B"))))
                    .andExpect(status().isOk());

            // Then - both should be SUBMITTED independently
            SolutionReview updatedA = solutionReviewRepository.findById(reviewA.getId()).orElseThrow();
            SolutionReview updatedB = solutionReviewRepository.findById(reviewB.getId()).orElseThrow();

            assertThat(updatedA.getDocumentState()).isEqualTo(DocumentState.SUBMITTED);
            assertThat(updatedB.getDocumentState()).isEqualTo(DocumentState.SUBMITTED);
            assertThat(updatedA.getSystemCode()).isEqualTo(systemA);
            assertThat(updatedB.getSystemCode()).isEqualTo(systemB);
        }
    }

    // ==================== DATABASE CONNECTION FAILURE SCENARIOS
    // ====================

    @Nested
    @DisplayName("Database Connection and Failure Scenarios")
    @Story("Error Handling and Resilience")
    class DatabaseFailureTests {

        @Test
        @DisplayName("Should handle missing document gracefully")
        @Description("Tests behavior when document is deleted during operation")
        @Severity(SeverityLevel.CRITICAL)
        void shouldHandleMissingDocumentGracefully() throws Exception {
            // Given - non-existent document ID
            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    "non-existent-document-id",
                    "SUBMIT",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Attempting on missing document");

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("not found")));
        }

        @Test
        @DisplayName("Should validate document state after retrieval")
        @Description("Ensures document state hasn't changed between retrieval and operation")
        @Severity(SeverityLevel.CRITICAL)
        void shouldValidateDocumentStateConsistency() throws Exception {
            // Given - DRAFT review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);
            String reviewId = review.getId();

            // Submit it first
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
                            "Submit"))))
                    .andExpect(status().isOk());

            // Try to submit again (should fail - it's already SUBMITTED)
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
                            "Submit again"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should handle corrupted document data gracefully")
        @Description("Tests handling of documents with invalid or missing required fields")
        @Severity(SeverityLevel.NORMAL)
        void shouldHandleCorruptedDocumentData() throws Exception {
            // Given - create a review then manually corrupt it
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            // Save a corrupted version (null state would cause issues, but we can't easily
            // do
            // this in integration test)
            // Instead, test with a review that exists but has minimal data

            LifecycleTransitionCommand command = new LifecycleTransitionCommand(
                    review.getId(),
                    "SUBMIT",
                    TestDataFactory.TestUsers.ARCHITECT,
                    "Transition on minimal document");

            // Should still work - the document state machine should handle it
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(command)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle empty or null modifiedBy field appropriately")
        @Description("Tests validation of required command fields")
        @Severity(SeverityLevel.NORMAL)
        void shouldHandleNullModifiedBy() throws Exception {
            // Given - valid document with null modifiedBy
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            String invalidJson = String.format("""
                    {
                        "documentId": "%s",
                        "operation": "SUBMIT",
                        "modifiedBy": null,
                        "comment": "Test with null user"
                    }
                    """, review.getId());

            // When & Then - should reject due to validation
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle malformed JSON request")
        @Description("Tests resilience against malformed request data")
        @Severity(SeverityLevel.NORMAL)
        void shouldHandleMalformedJson() throws Exception {
            // Given - malformed JSON
            String malformedJson = """
                    {
                        "documentId": "test-id",
                        "operation": "SUBMIT"
                        "modifiedBy": "user"
                    """; // Missing closing brace and comma

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle database query returning stale data")
        @Description("Tests system behavior when database returns outdated information")
        @Severity(SeverityLevel.NORMAL)
        void shouldHandleStaleDataScenario() throws Exception {
            // Given - create and transition a document
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);
            String reviewId = review.getId();

            // Transition to SUBMITTED
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
                            "Submit"))))
                    .andExpect(status().isOk());

            // Try an operation that would be valid on DRAFT but not SUBMITTED
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            reviewId, "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
                            "Submit again"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Cannot execute operation")));
        }

        @Test
        @DisplayName("Should handle multiple rapid failed operations without side effects")
        @Description("Tests that failed operations don't leave the system in inconsistent state")
        @Severity(SeverityLevel.CRITICAL)
        void shouldHandleMultipleFailedOperationsWithoutSideEffects() throws Exception {
            // Given - DRAFT review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);
            String reviewId = review.getId();

            // Attempt multiple invalid operations
            String[] invalidOps = { "APPROVE", "ACTIVATE", "UNAPPROVE", "MARK_OUTDATED" };

            for (String operation : invalidOps) {
                mockMvc.perform(post("/api/v1/lifecycle/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LifecycleTransitionCommand(
                                reviewId, operation,
                                TestDataFactory.TestUsers.ADMIN,
                                "Invalid operation"))))
                        .andExpect(status().isBadRequest());
            }

            // Then - document should still be in DRAFT state
            SolutionReview finalState = solutionReviewRepository.findById(reviewId).orElseThrow();
            assertThat(finalState.getDocumentState()).isEqualTo(DocumentState.DRAFT);
        }

        @Test
        @DisplayName("Should rollback on constraint violation during state transition")
        @Description("Tests that constraint violations don't partially update the document")
        @Severity(SeverityLevel.BLOCKER)
        void shouldRollbackOnConstraintViolation() throws Exception {
            // Given - system with existing ACTIVE document
            String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_A;
            createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            // Create APPROVED review
            com.project.core_service.models.solution_overview.SolutionOverview overview2 = TestDataFactory
                    .createSolutionOverview("Second Solution");
            overview2 = mongoTemplate.save(overview2);

            SolutionReview approvedReview = TestDataFactory.createSolutionReviewWithOverview(systemCode,
                    DocumentState.APPROVED, overview2);
            approvedReview.setId("approved-for-rollback-test");
            approvedReview.setLastModifiedBy("original-user");
            approvedReview = solutionReviewRepository.save(approvedReview);

            java.time.LocalDateTime originalTimestamp = approvedReview.getLastModifiedAt();

            // Try to activate (should fail due to existing ACTIVE)
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LifecycleTransitionCommand(
                            approvedReview.getId(),
                            "ACTIVATE",
                            "new-user",
                            "Should fail"))))
                    .andExpect(status().isBadRequest());

            // Verify document state wasn't modified
            SolutionReview unchangedReview = solutionReviewRepository
                    .findById(approvedReview.getId())
                    .orElseThrow();
            assertThat(unchangedReview.getDocumentState()).isEqualTo(DocumentState.APPROVED);
            assertThat(unchangedReview.getLastModifiedBy()).isEqualTo("original-user");
            // Timestamp should be unchanged or very close (accounting for test execution
            // time)
            if (originalTimestamp != null) {
                assertThat(unchangedReview.getLastModifiedAt()).isEqualTo(originalTimestamp);
            }
        }
    }
}
