package com.project.core_service.models.solution_overview;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

class ConcernTest {

    @Test
    void testConstructorAndGetters() {
        LocalDateTime followUpDate = LocalDateTime.now().plusDays(30);
        Concern concern = new Concern(
                "concern-001",
                ConcernType.RISK,
                "Data breach risk",
                "High impact on confidentiality",
                "Mitigation plan required",
                ConcernStatus.UNKNOWN,
                followUpDate);

        assertEquals("concern-001", concern.getId());
        assertEquals(ConcernType.RISK, concern.getType());
        assertEquals("Data breach risk", concern.getDescription());
        assertEquals("High impact on confidentiality", concern.getImpact());
        assertEquals("Mitigation plan required", concern.getDisposition());
        assertEquals(ConcernStatus.UNKNOWN, concern.getStatus());
        assertEquals(followUpDate, concern.getFollowUpDate());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        assertThrows(NullPointerException.class, this::createConcernWithNullType);
        assertThrows(NullPointerException.class, this::createConcernWithNullDescription);
        assertThrows(NullPointerException.class, this::createConcernWithNullImpact);
        assertThrows(NullPointerException.class, this::createConcernWithNullDisposition);
        assertThrows(NullPointerException.class, this::createConcernWithNullStatus);
        assertThrows(NullPointerException.class, this::createConcernWithNullFollowUpDate);
    }

    private LocalDateTime getTestDate() {
        return LocalDateTime.now().plusDays(7);
    }

    private void createConcernWithNullType() {
        new Concern("concern-002", null, "Description", "Impact", "Disposition", ConcernStatus.UNKNOWN,
                getTestDate());
    }

    private void createConcernWithNullDescription() {
        new Concern("concern-003", ConcernType.DECISION, null, "Impact", "Disposition", ConcernStatus.UNKNOWN,
                getTestDate());
    }

    private void createConcernWithNullImpact() {
        new Concern("concern-004", ConcernType.DEVIATION, "Description", null, "Disposition",
                ConcernStatus.UNKNOWN, getTestDate());
    }

    private void createConcernWithNullDisposition() {
        new Concern("concern-005", ConcernType.RISK, "Description", "Impact", null, ConcernStatus.UNKNOWN,
                getTestDate());
    }

    private void createConcernWithNullStatus() {
        new Concern("concern-006", ConcernType.RISK, "Description", "Impact", "Disposition", null,
                getTestDate());
    }

    private void createConcernWithNullFollowUpDate() {
        new Concern("concern-007", ConcernType.RISK, "Description", "Impact", "Disposition",
                ConcernStatus.UNKNOWN, null);
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime testDate = LocalDateTime.now().plusDays(7);

        Concern c1 = new Concern(
                "concern-007",
                ConcernType.RISK,
                "Data breach risk",
                "High impact on confidentiality",
                "Mitigation plan required",
                ConcernStatus.UNKNOWN,
                testDate);

        Concern c2 = new Concern(
                "concern-007",
                ConcernType.RISK,
                "Data breach risk",
                "High impact on confidentiality",
                "Mitigation plan required",
                ConcernStatus.UNKNOWN,
                testDate);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testToStringContainsKeyFields() {
        LocalDateTime testDate = LocalDateTime.now().plusDays(7);

        Concern concern = new Concern(
                "concern-008",
                ConcernType.RISK,
                "Data breach risk",
                "High impact on confidentiality",
                "Mitigation plan required",
                ConcernStatus.UNKNOWN,
                testDate);

        String toString = concern.toString();
        assertTrue(toString.contains("concern-008"));
        assertTrue(toString.contains("RISK"));
        assertTrue(toString.contains("Data breach risk"));
        assertTrue(toString.contains("High impact on confidentiality"));
        assertTrue(toString.contains("Mitigation plan required"));
        assertTrue(toString.contains("UNKNOWN"));
    }
}
