package com.project.core_service.models.solution_overview;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolutionOverviewTest {

    private SolutionDetails dummySolutionDetails() {
        return new SolutionDetails(
                "SolutionName",
                "ProjectName",
                "AWG001",
                "Architect",
                "PM",
                List.of("Partner1")
        );
    }

    private List<ApplicationUser> dummyApplicationUsers() {
        return List.of(ApplicationUser.EMPLOYEE, ApplicationUser.CUSTOMERS);
    }

    private List<Concern> dummyConcerns() {
        Concern dummyConcern = new Concern(
                "concern-001",
                ConcernType.RISK,
                "desc",
                "impact",
                "disposition",
                ConcernStatus.UNKNOWN
        );
        return List.of(dummyConcern);
    }

    @Nested
    class ConstructorTests {

        @Test
        void shouldConstructWithAllFields() {
            SolutionOverview overview = new SolutionOverview(
                    "id-001",
                    dummySolutionDetails(),
                    "ReviewerName",
                    ReviewType.NEW_BUILD,
                    ApprovalStatus.PENDING,
                    ReviewStatus.DRAFT,
                    "No conditions",
                    BusinessUnit.UNKNOWN,
                    BusinessDriver.OPERATIONAL_EFFICIENCY,
                    "Expected value outcome",
                    dummyApplicationUsers(),
                    dummyConcerns()
            );

            assertEquals("id-001", overview.getId());
            assertEquals(dummySolutionDetails(), overview.getSolutionDetails());
            assertEquals("ReviewerName", overview.getReviewedBy());
            assertEquals(ReviewType.NEW_BUILD, overview.getReviewType());
            assertEquals(ApprovalStatus.PENDING, overview.getApprovalStatus());
            assertEquals(ReviewStatus.DRAFT, overview.getReviewStatus());
            assertEquals("No conditions", overview.getConditions());
            assertEquals(BusinessUnit.UNKNOWN, overview.getBusinessUnit());
            assertEquals(BusinessDriver.OPERATIONAL_EFFICIENCY, overview.getBusinessDriver());
            assertEquals("Expected value outcome", overview.getValueOutcome());
            assertEquals(dummyApplicationUsers(), overview.getApplicationUsers());
            assertEquals(dummyConcerns(), overview.getConcerns());
        }

        @Test
        void shouldThrowExceptionWhenNullForNonNullFields() {
            SolutionDetails details = dummySolutionDetails();
            List<ApplicationUser> users = dummyApplicationUsers();

            assertThrows(NullPointerException.class, () -> new SolutionOverview(
                    "id-null1", null, "Reviewer", ReviewType.NEW_BUILD,
                    ApprovalStatus.PENDING, ReviewStatus.DRAFT,
                    "Conditions", BusinessUnit.UNKNOWN, BusinessDriver.RISK_MANAGEMENT,
                    "Value", users, null
            ));


            assertThrows(NullPointerException.class, () -> new SolutionOverview(
                    "id-null3", details,  null, ReviewType.NEW_BUILD,
                    ApprovalStatus.PENDING, ReviewStatus.DRAFT,
                    "Conditions", BusinessUnit.UNKNOWN, BusinessDriver.RISK_MANAGEMENT,
                    "Value", users, null
            ));

            assertThrows(NullPointerException.class, () -> new SolutionOverview(
                    "id-null4", details,  "Reviewer", null,
                    ApprovalStatus.PENDING, ReviewStatus.DRAFT,
                    "Conditions", BusinessUnit.UNKNOWN, BusinessDriver.RISK_MANAGEMENT,
                    "Value", users, null
            ));

            assertThrows(NullPointerException.class, () -> new SolutionOverview(
                    "id-null5", details,  "Reviewer", ReviewType.NEW_BUILD,
                    null, ReviewStatus.DRAFT,
                    "Conditions", BusinessUnit.UNKNOWN, BusinessDriver.RISK_MANAGEMENT,
                    "Value", users, null
            ));

            assertThrows(NullPointerException.class, () -> new SolutionOverview(
                    "id-null6", details,  "Reviewer", ReviewType.NEW_BUILD,
                    ApprovalStatus.PENDING, null,
                    "Conditions", BusinessUnit.UNKNOWN, BusinessDriver.RISK_MANAGEMENT,
                    "Value", users, null
            ));

            assertThrows(NullPointerException.class, () -> new SolutionOverview(
                    "id-null7", details,  "Reviewer", ReviewType.NEW_BUILD,
                    ApprovalStatus.PENDING, ReviewStatus.DRAFT,
                    "Conditions", null, BusinessDriver.RISK_MANAGEMENT,
                    "Value", users, null
            ));

            assertThrows(NullPointerException.class, () -> new SolutionOverview(
                    "id-null8", details,  "Reviewer", ReviewType.NEW_BUILD,
                    ApprovalStatus.PENDING, ReviewStatus.DRAFT,
                    "Conditions", BusinessUnit.UNKNOWN, null,
                    "Value", users, null
            ));

            assertThrows(NullPointerException.class, () -> new SolutionOverview(
                    "id-null9", details,  "Reviewer", ReviewType.NEW_BUILD,
                    ApprovalStatus.PENDING, ReviewStatus.DRAFT,
                    "Conditions", BusinessUnit.UNKNOWN, BusinessDriver.RISK_MANAGEMENT,
                    null, users, null
            ));

            assertThrows(NullPointerException.class, () -> new SolutionOverview(
                    "id-null10", details,  "Reviewer", ReviewType.NEW_BUILD,
                    ApprovalStatus.PENDING, ReviewStatus.DRAFT,
                    "Conditions", BusinessUnit.UNKNOWN, BusinessDriver.RISK_MANAGEMENT,
                    "Value", null, null
            ));
        }
    }

    @Nested
    class BuilderTests {

        @Test
        void builderDefaultsApplicationUsersToEmptyList() {
            SolutionOverview overview = SolutionOverview.builder()
                    .id("id-003")
                    .solutionDetails(dummySolutionDetails())
                    .reviewedBy("ReviewerName")
                    .reviewType(ReviewType.NEW_BUILD)
                    .approvalStatus(ApprovalStatus.PENDING)
                    .reviewStatus(ReviewStatus.DRAFT)
                    .businessUnit(BusinessUnit.UNKNOWN)
                    .businessDriver(BusinessDriver.OPERATIONAL_EFFICIENCY)
                    .valueOutcome("Some outcome")
                    .build();

            assertNotNull(overview.getApplicationUsers());
            assertTrue(overview.getApplicationUsers().isEmpty());
        }

        @Nested
        class NewDraftBuilderTests {
            @Test
            void shouldPrepopulateDraftValues() {
                SolutionOverview overview = SolutionOverview.newDraftBuilder()
                        .id("id-014")
                        .solutionDetails(dummySolutionDetails())
                        .reviewedBy("ReviewerDraft")
                        .businessUnit(BusinessUnit.UNKNOWN)
                        .businessDriver(BusinessDriver.RISK_MANAGEMENT)
                        .valueOutcome("Draft outcome")
                        .applicationUsers(dummyApplicationUsers())
                        .concerns(dummyConcerns())
                        .build();

                assertEquals(ReviewType.NEW_BUILD, overview.getReviewType());
                assertEquals(ApprovalStatus.PENDING, overview.getApprovalStatus());
                assertEquals(ReviewStatus.DRAFT, overview.getReviewStatus());
            }
        }

        @Nested
        class FromExistingBuilderTests {
            @Test
            void shouldCopyAllFields() {
                SolutionOverview original = new SolutionOverview(
                        "id-020",
                        dummySolutionDetails(),
                        "ReviewerX",
                        ReviewType.NEW_BUILD,
                        ApprovalStatus.PENDING,
                        ReviewStatus.DRAFT,
                        "Original conditions",
                        BusinessUnit.UNKNOWN,
                        BusinessDriver.OPERATIONAL_EFFICIENCY,
                        "Original outcome",
                        dummyApplicationUsers(),
                        dummyConcerns()
                );

                SolutionOverview copy = SolutionOverview.fromExisting(original).build();

                assertEquals(original, copy); // should be equal since all fields match
            }
        }

        @Nested
        class NewEnhancementBuilderTests {
            @Test
            void shouldOverrideWithEnhancementDefaults() {
                SolutionOverview original = new SolutionOverview(
                        "id-021",
                        dummySolutionDetails(),
                        "ReviewerEnh",
                        ReviewType.NEW_BUILD,
                        ApprovalStatus.PENDING,
                        ReviewStatus.DRAFT,
                        "Enh conditions",
                        BusinessUnit.UNKNOWN,
                        BusinessDriver.RISK_MANAGEMENT,
                        "Enh outcome",
                        dummyApplicationUsers(),
                        dummyConcerns()
                );

                SolutionOverview enhancement = SolutionOverview.newEnhancementBuilder(original).build();

                // copied fields
                assertEquals(original.getId(), enhancement.getId());
                assertEquals(original.getSolutionDetails(), enhancement.getSolutionDetails());
                assertEquals(original.getReviewedBy(), enhancement.getReviewedBy());
                assertEquals(original.getBusinessUnit(), enhancement.getBusinessUnit());
                assertEquals(original.getBusinessDriver(), enhancement.getBusinessDriver());
                assertEquals(original.getValueOutcome(), enhancement.getValueOutcome());

                // overridden defaults
                assertEquals(ReviewType.ENHANCEMENT, enhancement.getReviewType());
                assertEquals(ApprovalStatus.PENDING, enhancement.getApprovalStatus());
                assertEquals(ReviewStatus.DRAFT, enhancement.getReviewStatus());
            }
        }

        @Nested
        class NewApprovedBuilderTests {
            @Test
            void shouldStartWithApprovedDefaults() {
                SolutionOverview approved = SolutionOverview.newApprovedBuilder()
                        .id("id-022")
                        .solutionDetails(dummySolutionDetails())
                        .reviewedBy("ApproverY")
                        .businessUnit(BusinessUnit.UNKNOWN)
                        .businessDriver(BusinessDriver.REGULATORY)
                        .valueOutcome("Approved outcome")
                        .build();

                assertEquals(ReviewType.NEW_BUILD, approved.getReviewType());
                assertEquals(ApprovalStatus.APPROVED, approved.getApprovalStatus());
                assertEquals(ReviewStatus.COMPLETED, approved.getReviewStatus());

                // also ensure other fields got set
                assertEquals("id-022", approved.getId());
                assertEquals("ApproverY", approved.getReviewedBy());
                assertEquals("Approved outcome", approved.getValueOutcome());
            }
        }
    }


    @Nested
    class EqualityAndHashCodeTests {
        @Test
        void shouldBeEqualWhenAllFieldsMatch() {
            SolutionOverview so1 = new SolutionOverview(
                    "id-012", dummySolutionDetails(),
                    "ReviewerName", ReviewType.NEW_BUILD, ApprovalStatus.PENDING,
                    ReviewStatus.DRAFT, "No conditions",
                    BusinessUnit.UNKNOWN, BusinessDriver.OPERATIONAL_EFFICIENCY,
                    "Expected value outcome", dummyApplicationUsers(), dummyConcerns()
            );

            SolutionOverview so2 = new SolutionOverview(
                    "id-012", dummySolutionDetails(),
                    "ReviewerName", ReviewType.NEW_BUILD, ApprovalStatus.PENDING,
                    ReviewStatus.DRAFT, "No conditions",
                    BusinessUnit.UNKNOWN, BusinessDriver.OPERATIONAL_EFFICIENCY,
                    "Expected value outcome", dummyApplicationUsers(), dummyConcerns()
            );

            assertEquals(so1, so2);
            assertEquals(so1.hashCode(), so2.hashCode());
        }

        @Test
        void shouldNotBeEqualWhenDifferentIds() {
            SolutionOverview so1 = new SolutionOverview(
                    "id-A", dummySolutionDetails(),
                    "Reviewer", ReviewType.NEW_BUILD, ApprovalStatus.PENDING,
                    ReviewStatus.DRAFT, "Cond", BusinessUnit.UNKNOWN,
                    BusinessDriver.RISK_MANAGEMENT, "Outcome", dummyApplicationUsers(), dummyConcerns()
            );

            SolutionOverview so2 = new SolutionOverview(
                    "id-B", dummySolutionDetails(),
                    "Reviewer", ReviewType.NEW_BUILD, ApprovalStatus.PENDING,
                    ReviewStatus.DRAFT, "Cond", BusinessUnit.UNKNOWN,
                    BusinessDriver.RISK_MANAGEMENT, "Outcome", dummyApplicationUsers(), dummyConcerns()
            );

            assertNotEquals(so1, so2);
        }
    }

    @Nested
    class ToStringTests {
        @Test
        void shouldContainKeyFields() {
            SolutionOverview overview = new SolutionOverview(
                    "id-013", dummySolutionDetails(),
                    "ReviewerName", ReviewType.ENHANCEMENT, ApprovalStatus.APPROVED,
                    ReviewStatus.COMPLETED, "Conditions exist", BusinessUnit.UNKNOWN,
                    BusinessDriver.REGULATORY, "Outcome value",
                    dummyApplicationUsers(), dummyConcerns()
            );

            String toString = overview.toString();
            assertTrue(toString.contains("id-013"));
            assertTrue(toString.contains("ReviewerName"));
            assertTrue(toString.contains("ENHANCEMENT"));
            assertTrue(toString.contains("APPROVED"));
            assertTrue(toString.contains("COMPLETED"));
            assertTrue(toString.contains("Conditions exist"));
            assertTrue(toString.contains("UNKNOWN"));
            assertTrue(toString.contains("REGULATORY"));
            assertTrue(toString.contains("Outcome value"));
            assertTrue(toString.contains("EMPLOYEE"));
        }
    }
}
