package com.project.core_service.integration;

import com.project.core_service.dto.NewSolutionOverviewRequestDTO;
import com.project.core_service.models.solution_overview.*;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;

import java.util.List;
import java.util.ArrayList;

/**
 * Factory class for creating test data objects for integration tests.
 * 
 * <p>
 * This factory provides convenient methods to create test data with sensible
 * defaults while allowing customization when needed. All builders follow a
 * fluent
 * API pattern for ease of use.
 * 
 * <p>
 * Usage examples:
 * 
 * <pre>
 * // Create a simple solution overview DTO
 * NewSolutionOverviewRequestDTO dto = TestDataFactory.createSolutionOverviewDTO("TEST-SYS");
 * 
 * // Create a complete solution review
 * SolutionReview review = TestDataFactory.createSolutionReview("TEST-SYS", DocumentState.DRAFT);
 * </pre>
 */
public class TestDataFactory {

    private static int counter = 1;

    /**
     * Resets the internal counter used for generating unique test data.
     * Should be called before each test to ensure predictable test data.
     */
    public static void reset() {
        counter = 1;
    }

    /**
     * Creates a unique system code for testing.
     * 
     * @return a unique system code like "SYS-001", "SYS-002", etc.
     */
    public static String createSystemCode() {
        return String.format("SYS-%03d", counter++);
    }

    /**
     * Creates SolutionDetails with default test data.
     * 
     * @param solutionName the name of the solution
     * @return configured SolutionDetails
     */
    public static SolutionDetails createSolutionDetails(String solutionName) {
        return new SolutionDetails(
                solutionName,
                "Test Project " + counter,
                "SR-" + String.format("%03d", counter),
                "John Architect",
                "Jane Manager",
                "IT Partner Team");
    }

    /**
     * Creates a BusinessUnit with test data.
     * 
     * @return configured BusinessUnit
     */
    public static BusinessUnit createBusinessUnit() {
        return BusinessUnit.UNKNOWN;
    }

    /**
     * Creates a BusinessDriver with test data.
     * 
     * @return configured BusinessDriver
     */
    public static BusinessDriver createBusinessDriver() {
        return BusinessDriver.OPERATIONAL_EFFICIENCY;
    }

    /**
     * Creates an ApplicationUser with test data.
     * 
     * @param name the user's name (not used, for API compatibility)
     * @return configured ApplicationUser
     */
    public static ApplicationUser createApplicationUser() {
        return ApplicationUser.CUSTOMERS;
    }

    /**
     * Creates a list of application users for testing.
     * 
     * @return list of configured ApplicationUsers
     */
    public static List<ApplicationUser> createApplicationUsers() {
        List<ApplicationUser> users = new ArrayList<>();
        users.add(ApplicationUser.CUSTOMERS);
        users.add(ApplicationUser.EMPLOYEE);
        return users;
    }

    /**
     * Creates a Concern with test data.
     * 
     * @param description the concern description
     * @return configured Concern
     */
    public static Concern createConcern(String description) {
        return new Concern(
                null, // ID will be generated
                ConcernType.RISK,
                description,
                "High impact on system security and performance",
                "To be reviewed and addressed before activation",
                ConcernStatus.UNKNOWN,
                java.time.LocalDateTime.now().plusDays(7));
    }

    /**
     * Creates a list of concerns for testing.
     * 
     * @return list of configured Concerns
     */
    public static List<Concern> createConcerns() {
        List<Concern> concerns = new ArrayList<>();
        concerns.add(createConcern("Security review required before production deployment"));
        concerns.add(createConcern("Performance testing needed for high-load scenarios"));
        return concerns;
    }

    /**
     * Creates a NewSolutionOverviewRequestDTO with default test data.
     * 
     * @param solutionName the solution name
     * @return configured DTO
     */
    public static NewSolutionOverviewRequestDTO createSolutionOverviewDTO(String solutionName) {
        return new NewSolutionOverviewRequestDTO(
                createSolutionDetails(solutionName),
                createBusinessUnit(),
                createBusinessDriver(),
                "Expected to deliver significant cost savings and improved efficiency",
                createApplicationUsers(),
                new ArrayList<>() // No concerns initially
        );
    }

    /**
     * Creates a NewSolutionOverviewRequestDTO with concerns.
     * 
     * @param solutionName the solution name
     * @return configured DTO with concerns
     */
    public static NewSolutionOverviewRequestDTO createSolutionOverviewDTOWithConcerns(String solutionName) {
        return new NewSolutionOverviewRequestDTO(
                createSolutionDetails(solutionName),
                createBusinessUnit(),
                createBusinessDriver(),
                "Expected to deliver significant cost savings and improved efficiency",
                createApplicationUsers(),
                createConcerns());
    }

    /**
     * Creates a minimal NewSolutionOverviewRequestDTO with only required fields.
     * 
     * @param solutionName the solution name
     * @return configured DTO with minimal data
     */
    public static NewSolutionOverviewRequestDTO createMinimalSolutionOverviewDTO(String solutionName) {
        return new NewSolutionOverviewRequestDTO(
                createSolutionDetails(solutionName),
                createBusinessUnit(),
                createBusinessDriver(),
                "Minimal test data",
                null, // No application users
                null // No concerns
        );
    }

    /**
     * Creates a SolutionOverview entity with default test data.
     * Note: This returns an unsaved entity. You must save it to the database
     * before using it in a @DBRef relationship.
     * 
     * @param solutionName the solution name
     * @return configured SolutionOverview (unsaved)
     */
    public static SolutionOverview createSolutionOverview(String solutionName) {
        return SolutionOverview.newDraftBuilder()
                .solutionDetails(createSolutionDetails(solutionName))
                .businessUnit(createBusinessUnit())
                .businessDriver(createBusinessDriver())
                .valueOutcome("Expected to deliver significant benefits")
                .applicationUsers(createApplicationUsers())
                .concerns(new ArrayList<>())
                .build();
    }

    /**
     * Creates a SolutionReview entity with specified state.
     * 
     * IMPORTANT: This creates a SolutionReview WITHOUT saving the SolutionOverview.
     * You must save the SolutionOverview to the database first, then set it on the
     * review.
     * 
     * Usage in tests:
     * 
     * <pre>
     * SolutionOverview overview = TestDataFactory.createSolutionOverview("Test");
     * overview = solutionOverviewRepository.save(overview);
     * SolutionReview review = TestDataFactory.createSolutionReviewWithOverview(systemCode, state, overview);
     * review = solutionReviewRepository.save(review);
     * </pre>
     * 
     * @param systemCode    the system code
     * @param state         the document state
     * @param savedOverview the SAVED SolutionOverview entity (with ID)
     * @return configured SolutionReview (unsaved)
     */
    public static SolutionReview createSolutionReviewWithOverview(String systemCode, DocumentState state,
            SolutionOverview savedOverview) {
        String solutionName = savedOverview.getSolutionDetails().getSolutionName();

        return SolutionReview.builder()
                .id(solutionName.replace(" ", "-") + "-" + counter++) // Use counter for unique IDs
                .systemCode(systemCode)
                .documentState(state)
                .solutionOverview(savedOverview)
                .build();
    }

    /**
     * Creates a minimal SolutionReview for testing (with minimal dependencies).
     * This is kept for backward compatibility but use with caution.
     * 
     * @deprecated Use createSolutionReviewWithOverview() instead and save
     *             dependencies first
     */
    @Deprecated
    public static SolutionReview createSolutionReview(String systemCode, DocumentState state) {
        String solutionName = "Test Solution for " + systemCode;

        // Create a minimal review without @DBRef fields set
        // Tests must set and save the solutionOverview separately
        return SolutionReview.builder()
                .id(solutionName.replace(" ", "-") + "-1")
                .systemCode(systemCode)
                .documentState(state)
                .solutionOverview(null) // Must be set by test after saving
                .build();
    }

    /**
     * Creates a user identifier for testing.
     * 
     * @param name the user name
     * @return formatted user identifier
     */
    public static String createUserId(String name) {
        return name.toLowerCase().replace(" ", ".");
    }

    /**
     * Common test user identifiers
     */
    public static class TestUsers {
        public static final String ARCHITECT = "john.architect";
        public static final String REVIEWER = "jane.reviewer";
        public static final String ADMIN = "admin.user";
        public static final String BUSINESS_USER = "business.user";
    }

    /**
     * Common test system codes for consistent testing
     */
    public static class TestSystemCodes {
        public static final String SYSTEM_A = "SYS-A";
        public static final String SYSTEM_B = "SYS-B";
        public static final String SYSTEM_C = "SYS-C";
    }
}
