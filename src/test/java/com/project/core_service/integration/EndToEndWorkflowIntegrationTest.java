package com.project.core_service.integration;

import com.project.core_service.commands.LifecycleTransitionCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests simulating complete solution review workflows.
 * 
 * <p>
 * These tests simulate real-world scenarios where multiple operations are
 * performed
 * in sequence to complete business workflows. They verify that all components
 * work
 * together correctly from API request through to database persistence.
 * 
 * <h2>Test Scenarios Covered:</h2>
 * <ul>
 * <li>Complete new solution review workflow from creation to activation</li>
 * <li>Solution review enhancement workflow (creating new version from
 * active)</li>
 * <li>Review rejection and modification workflow</li>
 * <li>Multiple concurrent system reviews</li>
 * <li>System dependency tracking across lifecycle</li>
 * </ul>
 * 
 * @see BaseIntegrationTest
 */
@DisplayName("End-to-End Workflow Integration Tests")
@Epic("Solution Review Management")
@Feature("Complete Business Workflows")
class EndToEndWorkflowIntegrationTest extends BaseIntegrationTest {

	@Autowired
	private SolutionReviewRepository solutionReviewRepository;

	@BeforeEach
	@Step("Reset test data factory before each test")
	void setup() {
		TestDataFactory.reset();
	}

	// ==================== NEW SOLUTION REVIEW WORKFLOW ====================

	@Test
	@DisplayName("Complete Workflow: New Solution Review from Creation to Activation")
	@Description("Simulates the complete lifecycle of a new solution review: " +
			"Create → Submit → Approve → Activate")
	@Severity(SeverityLevel.BLOCKER)
	@Story("New Solution Review Workflow")
	void completeNewSolutionReviewWorkflow() {
		String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_A;
		String architect = TestDataFactory.TestUsers.ARCHITECT;
		String reviewer = TestDataFactory.TestUsers.REVIEWER;
		String admin = TestDataFactory.TestUsers.ADMIN;

		// ===== STEP 1: Create New Solution Review =====
		Allure.step("Step 1: Create new DRAFT solution review", () -> {
			NewSolutionOverviewRequestDTO dto = TestDataFactory
					.createSolutionOverviewDTO("Payment Processing System");

			mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemCode)
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(dto)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.documentState").value("DRAFT"))
					.andExpect(jsonPath("$.systemCode").value(systemCode))
					.andReturn();

			// Verify in database
			List<SolutionReview> reviews = solutionReviewRepository.findAllBySystemCode(systemCode);
			assertThat(reviews).hasSize(1);
			assertThat(reviews.get(0).getDocumentState()).isEqualTo(DocumentState.DRAFT);
		});

		// Get the created review
		SolutionReview review = solutionReviewRepository.findAllBySystemCode(systemCode).get(0);
		String reviewId = review.getId();

		// ===== STEP 2: Update Draft (optional modifications) =====
		Allure.step("Step 2: Update DRAFT with additional details", () -> {
			SolutionReviewDTO updateDto = new SolutionReviewDTO(review);
			updateDto.getSolutionOverview().setValueOutcome(
					"Enhanced value outcome: Reduce processing time by 50% and improve user satisfaction");

			mockMvc.perform(put("/api/v1/solution-review")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(updateDto)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.solutionOverview.valueOutcome")
							.value(containsString("Enhanced value outcome")));
		});

		// ===== STEP 3: Submit for Review =====
		Allure.step("Step 3: Submit solution review for approval", () -> {
			LifecycleTransitionCommand submitCommand = new LifecycleTransitionCommand(
					reviewId,
					"SUBMIT",
					architect,
					"Submitting payment processing system for architecture review");

			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(submitCommand)))
					.andExpect(status().isOk())
					.andExpect(content().string("Transition successful"));

			// Verify state
			SolutionReview updated = solutionReviewRepository.findById(reviewId).orElseThrow();
			assertThat(updated.getDocumentState()).isEqualTo(DocumentState.SUBMITTED);
			assertThat(updated.getLastModifiedBy()).isEqualTo(architect);
		});

		// ===== STEP 4: Add Concerns (reviewer feedback) =====
		Allure.step("Step 4: Reviewer adds concerns to submitted review", () -> {
			SolutionReview submitted = solutionReviewRepository.findById(reviewId).orElseThrow();
			SolutionReviewDTO concernDto = new SolutionReviewDTO(submitted);
			concernDto.getSolutionOverview().setConcerns(TestDataFactory.createConcerns());

			mockMvc.perform(put("/api/v1/solution-review/concerns")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(concernDto)))
					.andExpect(status().isOk());

			// Verify concerns added
			SolutionReview withConcerns = solutionReviewRepository.findById(reviewId).orElseThrow();
			assertThat(withConcerns.getSolutionOverview().getConcerns()).isNotEmpty();
		});

		// ===== STEP 5: Approve Review =====
		Allure.step("Step 5: Approve solution review after concerns addressed", () -> {
			LifecycleTransitionCommand approveCommand = new LifecycleTransitionCommand(
					reviewId,
					"APPROVE",
					reviewer,
					"All concerns addressed. Approved for activation.");

			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(approveCommand)))
					.andExpect(status().isOk());

			// Verify state
			SolutionReview approved = solutionReviewRepository.findById(reviewId).orElseThrow();
			assertThat(approved.getDocumentState()).isEqualTo(DocumentState.APPROVED);
			assertThat(approved.getLastModifiedBy()).isEqualTo(reviewer);
		});

		// ===== STEP 6: Activate Review =====
		Allure.step("Step 6: Activate approved solution review", () -> {
			LifecycleTransitionCommand activateCommand = new LifecycleTransitionCommand(
					reviewId,
					"ACTIVATE",
					admin,
					"Activating payment processing system solution");

			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(activateCommand)))
					.andExpect(status().isOk());

			// Verify final state
			SolutionReview active = solutionReviewRepository.findById(reviewId).orElseThrow();
			assertThat(active.getDocumentState()).isEqualTo(DocumentState.ACTIVE);
			assertThat(active.getLastModifiedBy()).isEqualTo(admin);
		});

		// ===== STEP 7: Verify in System Dependencies =====
		Allure.step("Step 7: Verify active review appears in system dependencies", () -> {
			mockMvc.perform(get("/api/v1/solution-review/system-dependencies")
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[*].systemCode", hasItem(systemCode)))
					.andExpect(jsonPath("$", hasSize(1)));
		});
	}

	// ==================== SOLUTION ENHANCEMENT WORKFLOW ====================

	@Test
	@DisplayName("Complete Workflow: Solution Enhancement from Existing Active")
	@Description("Simulates creating an enhancement version from an existing active solution, " +
			"going through full lifecycle while old version becomes outdated")
	@Severity(SeverityLevel.CRITICAL)
	@Story("Solution Enhancement Workflow")
	void completeSolutionEnhancementWorkflow() {
		String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_B;
		String architect = TestDataFactory.TestUsers.ARCHITECT;
		String admin = TestDataFactory.TestUsers.ADMIN;

		// ===== SETUP: Create and activate initial version =====
		SolutionReview v1 = Allure.step("Setup: Create initial ACTIVE version", () -> {
			return createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);
		});

		// ===== STEP 1: Create Enhancement Version =====
		String v2Id = Allure.step("Step 1: Create enhancement version from existing ACTIVE", () -> {
			mockMvc.perform(post("/api/v1/solution-review/existing/{systemCode}", systemCode)
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.documentState").value("DRAFT"))
					.andExpect(jsonPath("$.systemCode").value(systemCode))
					.andReturn();

			// Verify we now have 2 versions
			List<SolutionReview> versions = solutionReviewRepository.findAllBySystemCode(systemCode);
			assertThat(versions).hasSize(2);
			assertThat(versions.stream().filter(r -> r.getDocumentState() == DocumentState.DRAFT).count())
					.isEqualTo(1);
			assertThat(versions.stream().filter(r -> r.getDocumentState() == DocumentState.ACTIVE).count())
					.isEqualTo(1);

			// Return the new version ID
			return versions.stream()
					.filter(r -> r.getDocumentState() == DocumentState.DRAFT)
					.findFirst()
					.orElseThrow()
					.getId();
		});

		// ===== STEP 2: Submit Enhancement =====
		Allure.step("Step 2: Submit enhancement for review", () -> {
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							v2Id, "SUBMIT", architect, "Enhancement ready for review"))))
					.andExpect(status().isOk());
		});

		// ===== STEP 3: Approve Enhancement =====
		Allure.step("Step 3: Approve enhancement", () -> {
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							v2Id, "APPROVE", TestDataFactory.TestUsers.REVIEWER,
							"Enhancement approved"))))
					.andExpect(status().isOk());
		});

		// ===== STEP 4: Mark Old Version as Outdated =====
		Allure.step("Step 4: Mark old ACTIVE version as OUTDATED", () -> {
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							v1.getId(), "MARK_OUTDATED", admin,
							"Superseded by new version"))))
					.andExpect(status().isOk());

			// Verify old version is now OUTDATED
			SolutionReview oldVersion = solutionReviewRepository.findById(v1.getId()).orElseThrow();
			assertThat(oldVersion.getDocumentState()).isEqualTo(DocumentState.OUTDATED);
		});

		// ===== STEP 5: Activate New Version =====
		Allure.step("Step 5: Activate new enhancement version", () -> {
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							v2Id, "ACTIVATE", admin, "Activating enhancement"))))
					.andExpect(status().isOk());

			// Verify new version is ACTIVE
			SolutionReview newVersion = solutionReviewRepository.findById(v2Id).orElseThrow();
			assertThat(newVersion.getDocumentState()).isEqualTo(DocumentState.ACTIVE);
		});

		// ===== VERIFICATION: Check Final State =====
		Allure.step("Verification: Confirm final state - 1 ACTIVE, 1 OUTDATED", () -> {
			List<SolutionReview> allVersions = solutionReviewRepository.findAllBySystemCode(systemCode);
			assertThat(allVersions).hasSize(2);

			long activeCount = allVersions.stream()
					.filter(r -> r.getDocumentState() == DocumentState.ACTIVE)
					.count();
			long outdatedCount = allVersions.stream()
					.filter(r -> r.getDocumentState() == DocumentState.OUTDATED)
					.count();

			assertThat(activeCount).isEqualTo(1);
			assertThat(outdatedCount).isEqualTo(1);
		});
	}

	// ==================== REVIEW REJECTION AND MODIFICATION WORKFLOW
	// ====================

	@Test
	@DisplayName("Complete Workflow: Review Rejection and Re-submission")
	@Description("Simulates a review being rejected, modified, and re-submitted: " +
			"Create → Submit → Reject (back to DRAFT) → Modify → Re-submit → Approve → Activate")
	@Severity(SeverityLevel.NORMAL)
	@Story("Review Rejection Workflow")
	void reviewRejectionAndModificationWorkflow() {
		String systemCode = TestDataFactory.TestSystemCodes.SYSTEM_C;

		// ===== STEP 1: Create and Submit =====
		String reviewId = Allure.step("Step 1: Create and submit solution review", () -> {
			NewSolutionOverviewRequestDTO dto = TestDataFactory
					.createSolutionOverviewDTO("Initial Solution");

			mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemCode)
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(dto)))
					.andExpect(status().isCreated())
					.andReturn();

			SolutionReview review = solutionReviewRepository.findAllBySystemCode(systemCode).get(0);

			// Submit immediately
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							review.getId(), "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
							"Initial submission"))))
					.andExpect(status().isOk());

			return review.getId();
		});

		// ===== STEP 2: Reject (Remove Submission) =====
		Allure.step("Step 2: Reviewer requests changes (remove submission)", () -> {
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							reviewId, "REMOVE_SUBMISSION",
							TestDataFactory.TestUsers.REVIEWER,
							"Needs more detail on security architecture"))))
					.andExpect(status().isOk());

			// Verify back to DRAFT
			SolutionReview rejected = solutionReviewRepository.findById(reviewId).orElseThrow();
			assertThat(rejected.getDocumentState()).isEqualTo(DocumentState.DRAFT);
		});

		// ===== STEP 3: Modify Draft =====
		Allure.step("Step 3: Architect modifies draft based on feedback", () -> {
			SolutionReview draft = solutionReviewRepository.findById(reviewId).orElseThrow();
			SolutionReviewDTO updateDto = new SolutionReviewDTO(draft);
			updateDto.getSolutionOverview().setValueOutcome(
					"Updated with comprehensive security architecture details");

			mockMvc.perform(put("/api/v1/solution-review")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(updateDto)))
					.andExpect(status().isOk());
		});

		// ===== STEP 4: Re-submit =====
		Allure.step("Step 4: Re-submit modified solution review", () -> {
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							reviewId, "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
							"Re-submitting with updates"))))
					.andExpect(status().isOk());
		});

		// ===== STEP 5: Approve and Activate =====
		Allure.step("Step 5: Approve and activate", () -> {
			// Approve
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							reviewId, "APPROVE", TestDataFactory.TestUsers.REVIEWER,
							"Approved after revisions"))))
					.andExpect(status().isOk());

			// Activate
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							reviewId, "ACTIVATE", TestDataFactory.TestUsers.ADMIN,
							"Activating"))))
					.andExpect(status().isOk());

			// Verify final state
			SolutionReview active = solutionReviewRepository.findById(reviewId).orElseThrow();
			assertThat(active.getDocumentState()).isEqualTo(DocumentState.ACTIVE);
		});
	}

	// ==================== MULTIPLE CONCURRENT SYSTEMS WORKFLOW
	// ====================

	@Test
	@DisplayName("Complete Workflow: Multiple Systems Managed Concurrently")
	@Description("Simulates managing multiple system reviews simultaneously with different states")
	@Severity(SeverityLevel.NORMAL)
	@Story("Multiple Systems Management")
	void multipleConcurrentSystemsWorkflow() {
		// ===== Create reviews for 3 different systems in different states =====
		Allure.step("Setup: Create reviews for multiple systems", () -> {
			// System A: Create to ACTIVE
			String systemA = "SYS-A";
			NewSolutionOverviewRequestDTO dtoA = TestDataFactory.createSolutionOverviewDTO("System A");
			mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemA)
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(dtoA)))
					.andExpect(status().isCreated());
			SolutionReview reviewA = solutionReviewRepository.findAllBySystemCode(systemA).get(0);

			// Take System A through to ACTIVE
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							reviewA.getId(), "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
							"A-Submit"))));
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							reviewA.getId(), "APPROVE", TestDataFactory.TestUsers.REVIEWER,
							"A-Approve"))));
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							reviewA.getId(), "ACTIVATE", TestDataFactory.TestUsers.ADMIN,
							"A-Activate"))));

			// System B: Create and leave in SUBMITTED
			String systemB = "SYS-B";
			NewSolutionOverviewRequestDTO dtoB = TestDataFactory.createSolutionOverviewDTO("System B");
			mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemB)
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(dtoB)))
					.andExpect(status().isCreated());
			SolutionReview reviewB = solutionReviewRepository.findAllBySystemCode(systemB).get(0);
			mockMvc.perform(post("/api/v1/lifecycle/transition")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(new LifecycleTransitionCommand(
							reviewB.getId(), "SUBMIT", TestDataFactory.TestUsers.ARCHITECT,
							"B-Submit"))));

			// System C: Create and leave in DRAFT
			String systemC = "SYS-C";
			NewSolutionOverviewRequestDTO dtoC = TestDataFactory.createSolutionOverviewDTO("System C");
			mockMvc.perform(post("/api/v1/solution-review/{systemCode}", systemC)
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(dtoC)))
					.andExpect(status().isCreated());
		});

		// ===== Verify System View shows all 3 systems =====
		Allure.step("Verification: Check system view shows all systems", () -> {
			mockMvc.perform(get("/api/v1/solution-review/system-view")
					.param("page", "0")
					.param("size", "10")
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content", hasSize(3)))
					.andExpect(jsonPath("$.content[*].systemCode",
							containsInAnyOrder("SYS-A", "SYS-B", "SYS-C")));
		});

		// ===== Verify System Dependencies shows only ACTIVE =====
		Allure.step("Verification: Check system dependencies shows only ACTIVE", () -> {
			mockMvc.perform(get("/api/v1/solution-review/system-dependencies")
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(1)))
					.andExpect(jsonPath("$[0].systemCode").value("SYS-A"));
		});

		// ===== Verify filtering by state =====
		Allure.step("Verification: Filter reviews by document state", () -> {
			// Get DRAFT reviews
			mockMvc.perform(get("/api/v1/solution-review/by-state")
					.param("documentState", "DRAFT")
					.param("page", "0")
					.param("size", "10")
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content", hasSize(1)))
					.andExpect(jsonPath("$.content[0].systemCode").value("SYS-C"));

			// Get SUBMITTED reviews
			mockMvc.perform(get("/api/v1/solution-review/by-state")
					.param("documentState", "SUBMITTED")
					.param("page", "0")
					.param("size", "10")
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content", hasSize(1)))
					.andExpect(jsonPath("$.content[0].systemCode").value("SYS-B"));

			// Get ACTIVE reviews
			mockMvc.perform(get("/api/v1/solution-review/by-state")
					.param("documentState", "ACTIVE")
					.param("page", "0")
					.param("size", "10")
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content", hasSize(1)))
					.andExpect(jsonPath("$.content[0].systemCode").value("SYS-A"));
		});
	}
}
