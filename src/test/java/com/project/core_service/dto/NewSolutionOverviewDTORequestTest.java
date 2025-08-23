package com.project.core_service.dto;

import com.project.core_service.models.solution_overview.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;

class NewSolutionOverviewRequestDTOTest {

    private SolutionDetails dummySolutionDetails() {
        return new SolutionDetails(
                "SolutionX",
                "ProjectY",
                "AWG123",
                "ArchitectZ",
                "ManagerQ",
                "Nested1"
        );
    }

    private NewSolutionOverviewRequestDTO dummyDTO(
            SolutionDetails details,
            String valueOutcome
    ) {
        return new NewSolutionOverviewRequestDTO(
                details,
                BusinessUnit.UNKNOWN,
                BusinessDriver.RISK_MANAGEMENT,
                valueOutcome
        );
    }

    private void assertSolutionOverviewMapping(SolutionOverview overview, NewSolutionOverviewRequestDTO dto) {
        assertNotNull(overview);
        assertEquals(dto.getSolutionDetails(), overview.getSolutionDetails(), "solutionDetails");
        assertEquals(BusinessUnit.UNKNOWN, overview.getBusinessUnit());
        assertEquals(BusinessDriver.RISK_MANAGEMENT, overview.getBusinessDriver());
        assertEquals(dto.getValueOutcome(), overview.getValueOutcome());

        // Defaults from builder
        assertEquals(ReviewType.NEW_BUILD, overview.getReviewType());
        assertEquals(ApprovalStatus.PENDING, overview.getApprovalStatus());
        assertEquals(ReviewStatus.DRAFT, overview.getReviewStatus());
        assertNotNull(overview.getApplicationUsers());
        assertTrue(overview.getApplicationUsers().isEmpty());
    }

    @Nested
    @DisplayName("Mapping tests")
    class MappingTests {

        @Test
        void shouldMapAllFieldsCorrectly() {
            SolutionDetails details = dummySolutionDetails();
            String valueOutcome = "Increase resilience";

            NewSolutionOverviewRequestDTO dto = dummyDTO(details, valueOutcome);
            SolutionOverview overview = dto.toNewDraftEntity();

            assertSolutionOverviewMapping(overview, dto);
        }

        @Test
        void shouldHandleUnicodeAndLongStrings() {
            SolutionDetails details = dummySolutionDetails();
            String longValueOutcome = "🚀".repeat(1000);

            NewSolutionOverviewRequestDTO dto = dummyDTO(details, longValueOutcome);
            SolutionOverview overview = dto.toNewDraftEntity();

            assertEquals(longValueOutcome, overview.getValueOutcome());
        }

        @Test
        void repeatedInvocationsProduceDistinctInstances() {
            SolutionDetails details = dummySolutionDetails();
            NewSolutionOverviewRequestDTO dto = dummyDTO(details, "Outcome");

            SolutionOverview first = dto.toNewDraftEntity();
            SolutionOverview second = dto.toNewDraftEntity();

            assertSolutionOverviewMapping(first, dto);
            assertSolutionOverviewMapping(second, dto);
            assertNotSame(first, second, "Each call should produce a distinct instance");
        }
    }

    @Nested
    @DisplayName("Null safety tests")
    class NullSafetyTests {

        @Test
        void shouldThrowWhenSolutionDetailsIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new NewSolutionOverviewRequestDTO(
                            null,
                            BusinessUnit.UNKNOWN,
                            BusinessDriver.RISK_MANAGEMENT,
                            "Outcome"
                    )
            );
        }

        @Test
        void shouldThrowWhenBusinessUnitIsNull() {
            SolutionDetails details = dummySolutionDetails();
            assertThrows(NullPointerException.class, () ->
                    new NewSolutionOverviewRequestDTO(
                            details,
                            null,
                            BusinessDriver.RISK_MANAGEMENT,
                            "Outcome"
                    )
            );
        }

        @Test
        void shouldThrowWhenBusinessDriverIsNull() {
            SolutionDetails details = dummySolutionDetails();
            assertThrows(NullPointerException.class, () ->
                    new NewSolutionOverviewRequestDTO(
                            details,
                            BusinessUnit.UNKNOWN,
                            null,
                            "Outcome"
                    )
            );
        }

        @Test
        void shouldThrowWhenValueOutcomeIsNull() {
            SolutionDetails details = dummySolutionDetails();
            assertThrows(NullPointerException.class, () ->
                    new NewSolutionOverviewRequestDTO(
                            details,
                            BusinessUnit.UNKNOWN,
                            BusinessDriver.RISK_MANAGEMENT,
                            null
                    )
            );
        }
    }
}
