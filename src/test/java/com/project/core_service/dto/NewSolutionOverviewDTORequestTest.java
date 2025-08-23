package com.project.core_service.dto;

import com.project.core_service.models.solution_overview.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
                List.of("Nested1", "Nested2")
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

    @Nested
    @DisplayName("Defensive copy / mutation tests")
    class DefensiveCopyTests {

        @Test
        void mutatingSolutionDetailsListShouldNotAffectOverview() {
            List<String> nested = new ArrayList<>(List.of("N1", "N2"));
            SolutionDetails details = new SolutionDetails("S", "P", "AWG001", "Arch", "Mgr", nested);

            NewSolutionOverviewRequestDTO dto = dummyDTO(details, "Outcome");
            SolutionOverview overview = dto.toNewDraftEntity();

            nested.add("N3");

            assertEquals(List.of("N1", "N2"), overview.getSolutionDetails().getItBusinessPartners(),
                    "Nested partners expected to remain unchanged");
        }
    }
}
