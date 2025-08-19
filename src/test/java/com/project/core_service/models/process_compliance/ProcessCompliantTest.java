package com.project.core_service.models.process_compliance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProcessCompliantTest {

    @Test
    void testProcessCompliantConstructorAndGetters() {
        ProcessCompliant processCompliant = new ProcessCompliant(
                "pc-001",
                StandardGuideline.CRYPTOGRAPHY_STANDARDS,
                Compliant.TRUE,
                "Encryption must follow company cryptographic standards."
        );

        assertEquals("pc-001", processCompliant.getId());
        assertEquals(StandardGuideline.CRYPTOGRAPHY_STANDARDS, processCompliant.getStandardGuideline());
        assertEquals(Compliant.TRUE, processCompliant.getCompliant());
        assertEquals("Encryption must follow company cryptographic standards.", processCompliant.getDescription());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        // StandardGuideline null
        assertThrows(NullPointerException.class, () -> new ProcessCompliant(
                "pc-002", null, Compliant.TRUE, "Description"
        ));

        // Compliant null
        assertThrows(NullPointerException.class, () -> new ProcessCompliant(
                "pc-003", StandardGuideline.CRYPTOGRAPHY_STANDARDS, null, "Description"
        ));

        // Description null
        assertThrows(NullPointerException.class, () -> new ProcessCompliant(
                "pc-004", StandardGuideline.CRYPTOGRAPHY_STANDARDS, Compliant.TRUE, null
        ));
    }

    @Test
    void testProcessCompliantEqualsAndHashCode() {
        ProcessCompliant pc1 = new ProcessCompliant(
                "pc-001",
                StandardGuideline.CRYPTOGRAPHY_STANDARDS,
                Compliant.TRUE,
                "Encryption must follow company cryptographic standards."
        );

        ProcessCompliant pc2 = new ProcessCompliant(
                "pc-001",
                StandardGuideline.CRYPTOGRAPHY_STANDARDS,
                Compliant.TRUE,
                "Encryption must follow company cryptographic standards."
        );

        assertEquals(pc1, pc2);
        assertEquals(pc1.hashCode(), pc2.hashCode());
    }

    @Test
    void testProcessCompliantToString() {
        ProcessCompliant processCompliant = new ProcessCompliant(
                "pc-001",
                StandardGuideline.CRYPTOGRAPHY_STANDARDS,
                Compliant.TRUE,
                "Encryption must follow company cryptographic standards."
        );

        String toStringResult = processCompliant.toString();
        assertTrue(toStringResult.contains("pc-001"));
        assertTrue(toStringResult.contains("CRYPTOGRAPHY_STANDARDS"));
        assertTrue(toStringResult.contains("TRUE"));
        assertTrue(toStringResult.contains("Encryption must follow company cryptographic standards."));
    }
}
