package com.project.core_service.models.solution_overview;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConcernTest {

    @Test
    void testConstructorAndGetters() {
        Concern concern = new Concern(
                "concern-001",
                ConcernType.RISK,
                "Data breach risk",
                "High impact on confidentiality",
                "Mitigation plan required",
                ConcernStatus.UNKNOWN,
                1
        );

        assertEquals("concern-001", concern.getId());
        assertEquals(ConcernType.RISK, concern.getType());
        assertEquals("Data breach risk", concern.getDescription());
        assertEquals("High impact on confidentiality", concern.getImpact());
        assertEquals("Mitigation plan required", concern.getDisposition());
        assertEquals(ConcernStatus.UNKNOWN, concern.getStatus());
        assertEquals(1, concern.getVersion());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        assertThrows(NullPointerException.class, () -> new Concern(
                "concern-002",
                null, // type
                "Description",
                "Impact",
                "Disposition",
                ConcernStatus.UNKNOWN,
                1
        ));

        assertThrows(NullPointerException.class, () -> new Concern(
                "concern-003",
                ConcernType.DECISION,
                null, // description
                "Impact",
                "Disposition",
                ConcernStatus.UNKNOWN,
                1
        ));

        assertThrows(NullPointerException.class, () -> new Concern(
                "concern-004",
                ConcernType.DEVIATION,
                "Description",
                null, // impact
                "Disposition",
                ConcernStatus.UNKNOWN,
                1
        ));

        assertThrows(NullPointerException.class, () -> new Concern(
                "concern-005",
                ConcernType.RISK,
                "Description",
                "Impact",
                null, // disposition
                ConcernStatus.UNKNOWN,
                1
        ));

        assertThrows(NullPointerException.class, () -> new Concern(
                "concern-006",
                ConcernType.RISK,
                "Description",
                "Impact",
                "Disposition",
                null, // status
                1
        ));
    }

    @Test
    void testEqualsAndHashCode() {
        Concern c1 = new Concern(
                "concern-007",
                ConcernType.RISK,
                "Data breach risk",
                "High impact on confidentiality",
                "Mitigation plan required",
                ConcernStatus.UNKNOWN,
                1
        );

        Concern c2 = new Concern(
                "concern-007",
                ConcernType.RISK,
                "Data breach risk",
                "High impact on confidentiality",
                "Mitigation plan required",
                ConcernStatus.UNKNOWN,
                1
        );

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testToStringContainsKeyFields() {
        Concern concern = new Concern(
                "concern-008",
                ConcernType.RISK,
                "Data breach risk",
                "High impact on confidentiality",
                "Mitigation plan required",
                ConcernStatus.UNKNOWN,
                1
        );

        String toString = concern.toString();
        assertTrue(toString.contains("concern-008"));
        assertTrue(toString.contains("RISK"));
        assertTrue(toString.contains("Data breach risk"));
        assertTrue(toString.contains("High impact on confidentiality"));
        assertTrue(toString.contains("Mitigation plan required"));
        assertTrue(toString.contains("UNKNOWN"));
    }
}
