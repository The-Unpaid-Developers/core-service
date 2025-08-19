package com.project.core_service.models.solution_overview;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class SolutionOverviewTest {

    private SolutionDetails dummySolutionDetails() {
        return new SolutionDetails(
                "SolutionName",
                "SYS001",
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

    private List<String> dummyBusinessPartners() {
        return List.of("PartnerA", "PartnerB");
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

    @Test
    void testConstructorAndGetters() {
        SolutionOverview overview = new SolutionOverview(
                "id-001",
                dummySolutionDetails(),
                dummyBusinessPartners(),
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
        assertEquals(dummyBusinessPartners(), overview.getItBusinessPartners());
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
        SolutionDetails solutionDetails = dummySolutionDetails();
        List<String> businessPartners = dummyBusinessPartners();
        List<ApplicationUser> applicationUsers = dummyApplicationUsers();

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-002",
                null,
                businessPartners,
                "Reviewer",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "Conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.RISK_MANAGEMENT,
                "Value",
                applicationUsers,
                null
        ));

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-003",
                solutionDetails,
                null,
                "Reviewer",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "Conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.RISK_MANAGEMENT,
                "Value",
                applicationUsers,
                null
        ));

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-004",
                solutionDetails,
                businessPartners,
                null,
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "Conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.RISK_MANAGEMENT,
                "Value",
                applicationUsers,
                null
        ));

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-005",
                solutionDetails,
                businessPartners,
                "Reviewer",
                null,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "Conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.RISK_MANAGEMENT,
                "Value",
                applicationUsers,
                null
        ));

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-006",
                solutionDetails,
                businessPartners,
                "Reviewer",
                ReviewType.NEW_BUILD,
                null,
                ReviewStatus.DRAFT,
                "Conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.RISK_MANAGEMENT,
                "Value",
                applicationUsers,
                null
        ));

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-007",
                solutionDetails,
                businessPartners,
                "Reviewer",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                null,
                "Conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.RISK_MANAGEMENT,
                "Value",
                applicationUsers,
                null
        ));

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-008",
                solutionDetails,
                businessPartners,
                "Reviewer",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "Conditions",
                null,
                BusinessDriver.RISK_MANAGEMENT,
                "Value",
                applicationUsers,
                null
        ));

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-009",
                solutionDetails,
                businessPartners,
                "Reviewer",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "Conditions",
                BusinessUnit.UNKNOWN,
                null,
                "Value",
                applicationUsers,
                null
        ));

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-010",
                solutionDetails,
                businessPartners,
                "Reviewer",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "Conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.RISK_MANAGEMENT,
                null,
                applicationUsers,
                null
        ));

        assertThrows(NullPointerException.class, () -> new SolutionOverview(
                "id-011",
                solutionDetails,
                businessPartners,
                "Reviewer",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "Conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.RISK_MANAGEMENT,
                "Value",
                null,
                null
        ));
    }

    @Test
    void testEqualsAndHashCode() {
        SolutionOverview so1 = new SolutionOverview(
                "id-012",
                dummySolutionDetails(),
                dummyBusinessPartners(),
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

        SolutionOverview so2 = new SolutionOverview(
                "id-012",
                dummySolutionDetails(),
                dummyBusinessPartners(),
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

        assertEquals(so1, so2);
        assertEquals(so1.hashCode(), so2.hashCode());
    }

    @Test
    void testToStringContainsKeyFields() {
        SolutionOverview overview = new SolutionOverview(
                "id-013",
                dummySolutionDetails(),
                dummyBusinessPartners(),
                "ReviewerName",
                ReviewType.ENHANCEMENT,
                ApprovalStatus.APPROVED,
                ReviewStatus.COMPLETED,
                "Conditions exist",
                BusinessUnit.UNKNOWN,
                BusinessDriver.REGULATORY,
                "Outcome value",
                dummyApplicationUsers(),
                dummyConcerns()
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

