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
public class LifecycleControllerIntegrationTest extends BaseIntegrationTest {

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
                        assertThat(updated.getLastModifiedAt()).isAfter(review.getLastModifiedAt());
                }
        }
}
