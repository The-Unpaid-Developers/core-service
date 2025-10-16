package com.project.core_service.integration;

import com.project.core_service.dto.NewSolutionOverviewRequestDTO;
import com.project.core_service.dto.SolutionReviewDTO;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.repositories.SolutionReviewRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for
 * {@link com.project.core_service.controllers.SolutionReviewController}.
 * 
 * <p>
 * These tests verify the complete end-to-end flow of solution review
 * operations,
 * including controller layer, service layer, and database persistence.
 * 
 * <h2>Test Scenarios Covered:</h2>
 * <ul>
 * <li>Creating new solution reviews</li>
 * <li>Retrieving solution reviews (by ID, by system code, paginated)</li>
 * <li>Updating solution reviews and concerns</li>
 * <li>Deleting solution reviews</li>
 * <li>Filtering by document state</li>
 * <li>System dependency queries</li>
 * <li>Error handling and validation</li>
 * </ul>
 * 
 * @see com.project.core_service.controllers.SolutionReviewController
 * @see BaseIntegrationTest
 */
@DisplayName("Solution Review Controller Integration Tests")
@Epic("Solution Review Management")
@Feature("Solution Review CRUD Operations")
class SolutionReviewControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SolutionReviewRepository solutionReviewRepository;

    @BeforeEach
    @Step("Reset test data factory before each test")
    void setup() {
        TestDataFactory.reset();
    }

    // ==================== CREATE OPERATIONS ====================

    @Nested
    @DisplayName("POST /api/v1/solution-review/{systemCode} - Create Solution Review")
    @Story("Create Solution Review")
    class CreateSolutionReviewTests {

        @Test
        @DisplayName("Should create a new solution review with valid data")
        @Description("Creates a new DRAFT solution review for a system that has no existing reviews")
        @Severity(SeverityLevel.CRITICAL)
        void shouldCreateNewSolutionReview() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            NewSolutionOverviewRequestDTO dto = TestDataFactory.createSolutionOverviewDTO("Test Solution");

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.systemCode").value(systemCode))
                    .andExpect(jsonPath("$.documentState").value("DRAFT"))
                    .andExpect(jsonPath("$.solutionOverview").exists())
                    .andExpect(jsonPath("$.solutionOverview.solutionDetails.solutionName").value("Test Solution"));

            // Verify in database
            List<SolutionReview> reviews = solutionReviewRepository.findAllBySystemCode(systemCode);
            assertThat(reviews).hasSize(1);
            assertThat(reviews.get(0).getDocumentState()).isEqualTo(DocumentState.DRAFT);
        }

        @Test
        @DisplayName("Should create solution review with minimal data")
        @Description("Verifies that a solution review can be created with only required fields")
        @Severity(SeverityLevel.NORMAL)
        void shouldCreateSolutionReviewWithMinimalData() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            NewSolutionOverviewRequestDTO dto = TestDataFactory.createMinimalSolutionOverviewDTO("Minimal Solution");

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.systemCode").value(systemCode))
                    .andExpect(jsonPath("$.documentState").value("DRAFT"));

            // Verify in database
            Optional<SolutionReview> saved = solutionReviewRepository.findAllBySystemCode(systemCode)
                    .stream().findFirst();
            assertThat(saved).isPresent();
        }

        @Test
        @DisplayName("Should fail to create solution review when another DRAFT exists")
        @Description("Validates the exclusive state constraint - only one DRAFT per system")
        @Severity(SeverityLevel.CRITICAL)
        void shouldFailToCreateWhenDraftExists() throws Exception {
            // Given - create first DRAFT
            String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_A;
            NewSolutionOverviewRequestDTO dto1 = TestDataFactory.createSolutionOverviewDTO("First Solution");

            mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto1)))
                    .andExpect(status().isCreated());

            // When & Then - try to create another DRAFT
            NewSolutionOverviewRequestDTO dto2 = TestDataFactory.createSolutionOverviewDTO("Second Solution");

            mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto2)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("exclusive states")));
        }

        @Test
        @DisplayName("Should fail with 400 when request body is invalid")
        @Description("Validates request validation for missing required fields")
        @Severity(SeverityLevel.NORMAL)
        void shouldFailWithInvalidRequestBody() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            String invalidJson = "{}";

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/solution-review/existing/{systemCode} - Create from Existing")
    @Story("Create Solution Review from Existing")
    class CreateFromExistingTests {

        @Test
        @DisplayName("Should create new DRAFT from existing ACTIVE solution")
        @Description("Creates a new DRAFT version based on an existing ACTIVE solution review")
        @Severity(SeverityLevel.CRITICAL)
        void shouldCreateFromExistingActive() throws Exception {
            // Given - create and activate a solution review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview activeReview = createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/existing/{systemCode}", systemCode)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.systemCode").value(systemCode))
                    .andExpect(jsonPath("$.documentState").value("DRAFT"))
                    .andExpect(jsonPath("$.id").value(not(activeReview.getId())));

            // Verify we now have 2 reviews for this system
            List<SolutionReview> reviews = solutionReviewRepository.findAllBySystemCode(systemCode);
            assertThat(reviews).hasSize(2);
        }

        @Test
        @DisplayName("Should fail when no ACTIVE solution exists")
        @Description("Validates that creating from existing requires an ACTIVE version to exist")
        @Severity(SeverityLevel.NORMAL)
        void shouldFailWhenNoActiveExists() throws Exception {
            // Given - system with no ACTIVE review
            String systemCode = TestDataFactory.createSystemCode();

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/existing/{systemCode}", systemCode)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("No ACTIVE solution review found")));
        }
    }

    // ==================== READ OPERATIONS ====================

    @Nested
    @DisplayName("GET Operations - Retrieve Solution Reviews")
    @Story("Retrieve Solution Reviews")
    class RetrieveSolutionReviewTests {

        @Test
        @DisplayName("Should retrieve solution review by ID")
        @Description("Fetches a specific solution review using its unique identifier")
        @Severity(SeverityLevel.CRITICAL)
        void shouldRetrieveSolutionReviewById() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            // When & Then
            mockMvc.perform(get("/api/v1/solution-review/{id}", review.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(review.getId()))
                    .andExpect(jsonPath("$.systemCode").value(systemCode))
                    .andExpect(jsonPath("$.documentState").value("DRAFT"));
        }

        @Test
        @DisplayName("Should return 404 when solution review not found")
        @Description("Validates proper error handling when requesting a non-existent review")
        @Severity(SeverityLevel.NORMAL)
        void shouldReturn404WhenNotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/solution-review/{id}", "non-existent-id")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should retrieve all solution reviews")
        @Description("Fetches all solution reviews without pagination")
        @Severity(SeverityLevel.NORMAL)
        void shouldRetrieveAllSolutionReviews() throws Exception {
            // Given - create multiple reviews
            createAndSaveSolutionReview("SYS-001", DocumentState.DRAFT);
            createAndSaveSolutionReview("SYS-002", DocumentState.SUBMITTED);
            createAndSaveSolutionReview("SYS-003", DocumentState.ACTIVE);

            // When & Then
            mockMvc.perform(get("/api/v1/solution-review")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[*].systemCode", containsInAnyOrder("SYS-001", "SYS-002", "SYS-003")));
        }

        @Test
        @DisplayName("Should retrieve solution reviews with pagination")
        @Description("Fetches paginated solution reviews with specified page and size")
        @Severity(SeverityLevel.NORMAL)
        void shouldRetrieveSolutionReviewsWithPagination() throws Exception {
            // Given - create 5 reviews
            for (int i = 1; i <= 5; i++) {
                createAndSaveSolutionReview("SYS-00" + i, DocumentState.DRAFT);
            }

            // When & Then - get page 0 with size 2
            mockMvc.perform(get("/api/v1/solution-review/paging")
                    .param("page", "0")
                    .param("size", "2")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(5))
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        @DisplayName("Should retrieve solution reviews by system code")
        @Description("Fetches all reviews for a specific system")
        @Severity(SeverityLevel.CRITICAL)
        void shouldRetrieveSolutionReviewsBySystemCode() throws Exception {
            // Given
            String systemCode = "TEST-SYSTEM";
            createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);
            createAndSaveSolutionReview(systemCode, DocumentState.OUTDATED);
            createAndSaveSolutionReview("OTHER-SYSTEM", DocumentState.ACTIVE);

            // When & Then
            mockMvc.perform(get("/api/v1/solution-review/system")
                    .param("systemCode", systemCode)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].systemCode", everyItem(is(systemCode))));
        }

        @Test
        @DisplayName("Should retrieve solution reviews by document state")
        @Description("Filters solution reviews by their document state (DRAFT, SUBMITTED, etc.)")
        @Severity(SeverityLevel.NORMAL)
        void shouldRetrieveSolutionReviewsByDocumentState() throws Exception {
            // Given
            createAndSaveSolutionReview("SYS-001", DocumentState.DRAFT);
            createAndSaveSolutionReview("SYS-002", DocumentState.DRAFT);
            createAndSaveSolutionReview("SYS-003", DocumentState.SUBMITTED);

            // When & Then
            mockMvc.perform(get("/api/v1/solution-review/by-state")
                    .param("documentState", "DRAFT")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[*].documentState", everyItem(is("DRAFT"))));
        }

        @Test
        @DisplayName("Should fail with invalid document state")
        @Description("Validates error handling for invalid document state values")
        @Severity(SeverityLevel.NORMAL)
        void shouldFailWithInvalidDocumentState() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/solution-review/by-state")
                    .param("documentState", "INVALID_STATE")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Invalid document state")));
        }

        @Test
        @DisplayName("Should retrieve system dependencies")
        @Description("Fetches active solution reviews with system dependency information")
        @Severity(SeverityLevel.NORMAL)
        void shouldRetrieveSystemDependencies() throws Exception {
            // Given - create ACTIVE reviews only
            createAndSaveSolutionReview("SYS-A", DocumentState.ACTIVE);
            createAndSaveSolutionReview("SYS-B", DocumentState.ACTIVE);
            createAndSaveSolutionReview("SYS-C", DocumentState.DRAFT);

            // When & Then - should only return ACTIVE reviews
            mockMvc.perform(get("/api/v1/solution-review/system-dependencies")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].systemCode", containsInAnyOrder("SYS-A", "SYS-B")));
        }

        @Test
        @DisplayName("Should retrieve business capabilities for diagram")
        @Description("Fetches active solution reviews with business capability information")
        @Severity(SeverityLevel.NORMAL)
        void shouldRetrieveBusinessCapabilitiesForDiagram() throws Exception {
            // Given - create ACTIVE reviews only
            createAndSaveSolutionReview("SYS-A", DocumentState.ACTIVE);
            createAndSaveSolutionReview("SYS-B", DocumentState.ACTIVE);
            createAndSaveSolutionReview("SYS-C", DocumentState.DRAFT);

            // When & Then - should only return ACTIVE reviews
            mockMvc.perform(get("/api/v1/solution-review/business-capabilities")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].systemCode", containsInAnyOrder("SYS-A", "SYS-B")));
        }

        @Test
        @DisplayName("Should retrieve paginated system view")
        @Description("Fetches representative reviews for each system with pagination")
        @Severity(SeverityLevel.NORMAL)
        void shouldRetrievePaginatedSystemView() throws Exception {
            // Given - create reviews for different systems
            createAndSaveSolutionReview("SYS-001", DocumentState.ACTIVE);
            createAndSaveSolutionReview("SYS-002", DocumentState.ACTIVE);
            createAndSaveSolutionReview("SYS-003", DocumentState.DRAFT);

            // When & Then
            mockMvc.perform(get("/api/v1/solution-review/system-view")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)));
        }
    }

    // ==================== UPDATE OPERATIONS ====================

    @Nested
    @DisplayName("PUT Operations - Update Solution Reviews")
    @Story("Update Solution Reviews")
    class UpdateSolutionReviewTests {

        @Test
        @DisplayName("Should update a DRAFT solution review")
        @Description("Updates an existing DRAFT solution review with new data")
        @Severity(SeverityLevel.CRITICAL)
        void shouldUpdateDraftSolutionReview() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            // Modify the review
            SolutionReviewDTO dto = new SolutionReviewDTO(review);
            dto.getSolutionOverview().setValueOutcome("Updated value outcome");

            // When & Then
            mockMvc.perform(put("/api/v1/solution-review")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(review.getId()))
                    .andExpect(jsonPath("$.solutionOverview.valueOutcome").value("Updated value outcome"));

            // Verify in database
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getSolutionOverview().getValueOutcome()).isEqualTo("Updated value outcome");
        }

        @Test
        @DisplayName("Should fail to update non-DRAFT solution review")
        @Description("Validates that only DRAFT reviews can be fully updated")
        @Severity(SeverityLevel.CRITICAL)
        void shouldFailToUpdateNonDraftReview() throws Exception {
            // Given - create a SUBMITTED review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.SUBMITTED);

            SolutionReviewDTO dto = new SolutionReviewDTO(review);
            dto.getSolutionOverview().setValueOutcome("Attempted update");

            // When & Then
            mockMvc.perform(put("/api/v1/solution-review")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value(containsString("Only DRAFT reviews can be modified")));
        }

        @Test
        @DisplayName("Should update concerns in SUBMITTED solution review")
        @Description("Updates only the concerns field in a SUBMITTED review")
        @Severity(SeverityLevel.NORMAL)
        void shouldUpdateConcernsInSubmittedReview() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.SUBMITTED);

            SolutionReviewDTO dto = new SolutionReviewDTO(review);
            dto.getSolutionOverview().setConcerns(TestDataFactory.createConcerns());

            // When & Then
            mockMvc.perform(put("/api/v1/solution-review/concerns")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(review.getId()));

            // Verify concerns were updated
            SolutionReview updated = solutionReviewRepository.findById(review.getId()).orElseThrow();
            assertThat(updated.getSolutionOverview().getConcerns()).isNotEmpty();
        }

        @Test
        @DisplayName("Should fail to update concerns in non-SUBMITTED review")
        @Description("Validates that concerns can only be updated for SUBMITTED reviews")
        @Severity(SeverityLevel.NORMAL)
        void shouldFailToUpdateConcernsInNonSubmittedReview() throws Exception {
            // Given - DRAFT review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            SolutionReviewDTO dto = new SolutionReviewDTO(review);
            dto.getSolutionOverview().setConcerns(TestDataFactory.createConcerns());

            // When & Then
            mockMvc.perform(put("/api/v1/solution-review/concerns")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value(containsString("Only SUBMITTED reviews can have concerns updated")));
        }
    }

    // ==================== DELETE OPERATIONS ====================

    @Nested
    @DisplayName("DELETE /api/v1/solution-review/{id} - Delete Solution Review")
    @Story("Delete Solution Review")
    class DeleteSolutionReviewTests {

        @Test
        @DisplayName("Should delete a DRAFT solution review")
        @Description("Deletes a DRAFT solution review and all its related entities")
        @Severity(SeverityLevel.CRITICAL)
        void shouldDeleteDraftSolutionReview() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            // When & Then
            mockMvc.perform(delete("/api/v1/solution-review/{id}", review.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // Verify deletion
            Optional<SolutionReview> deleted = solutionReviewRepository.findById(review.getId());
            assertThat(deleted).isEmpty();
        }

        @Test
        @DisplayName("Should fail to delete non-DRAFT solution review")
        @Description("Validates that only DRAFT reviews can be deleted")
        @Severity(SeverityLevel.CRITICAL)
        void shouldFailToDeleteNonDraftReview() throws Exception {
            // Given - ACTIVE review
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            // When & Then
            mockMvc.perform(delete("/api/v1/solution-review/{id}", review.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value(containsString("Only DRAFT reviews can be deleted")));

            // Verify it still exists
            Optional<SolutionReview> stillExists = solutionReviewRepository.findById(review.getId());
            assertThat(stillExists).isPresent();
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent review")
        @Description("Validates error handling when attempting to delete a non-existent review")
        @Severity(SeverityLevel.NORMAL)
        void shouldReturn404WhenDeletingNonExistentReview() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/v1/solution-review/{id}", "non-existent-id")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
