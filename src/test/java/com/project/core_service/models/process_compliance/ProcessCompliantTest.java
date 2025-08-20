package com.project.core_service.models.process_compliance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProcessCompliantTest {
    @Test
    void builderSetsFieldsCorrectly() {
        ProcessCompliant process = ProcessCompliant.builder()
                .id("pc-123")
                .standardGuideline(StandardGuideline.ACCESS_CONTROL_STANDARDS)
                .compliant(Compliant.TRUE)
                .description("Access control checks in place")
                .version(1)
                .build();

        assertEquals("pc-123", process.getId());
        assertEquals(StandardGuideline.ACCESS_CONTROL_STANDARDS, process.getStandardGuideline());
        assertEquals(Compliant.TRUE, process.getCompliant());
        assertEquals("Access control checks in place", process.getDescription());
        assertEquals(1, process.getVersion());
    }

    @Test
    void nonNullFieldsShouldThrowOnNull() {
        assertThrows(NullPointerException.class, () -> {
            ProcessCompliant.builder()
                    .id("pc-456")
                    .standardGuideline(null) // should fail due to @NonNull
                    .compliant(Compliant.TRUE)
                    .description("Missing guideline should break")
                    .build();
        });
    }
    @Test
    void testProcessCompliantConstructorAndGetters() {
        ProcessCompliant processCompliant = new ProcessCompliant(
                "pc-001",
                StandardGuideline.CRYPTOGRAPHY_STANDARDS,
                Compliant.TRUE,
                "Encryption must follow company cryptographic standards.",
                1
        );

        assertEquals("pc-001", processCompliant.getId());
        assertEquals(StandardGuideline.CRYPTOGRAPHY_STANDARDS, processCompliant.getStandardGuideline());
        assertEquals(Compliant.TRUE, processCompliant.getCompliant());
        assertEquals("Encryption must follow company cryptographic standards.", processCompliant.getDescription());
        assertEquals(1, processCompliant.getVersion());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        // StandardGuideline null
        assertThrows(NullPointerException.class, () -> new ProcessCompliant(
                "pc-002", null, Compliant.TRUE, "Description", 1
        ));

        // Compliant null
        assertThrows(NullPointerException.class, () -> new ProcessCompliant(
                "pc-003", StandardGuideline.CRYPTOGRAPHY_STANDARDS, null, "Description", 1
        ));

        // Description null
        assertThrows(NullPointerException.class, () -> new ProcessCompliant(
                "pc-004", StandardGuideline.CRYPTOGRAPHY_STANDARDS, Compliant.TRUE, null, 1
        ));
    }

    @Test
    void testProcessCompliantEqualsAndHashCode() {
        ProcessCompliant pc1 = new ProcessCompliant(
                "pc-001",
                StandardGuideline.CRYPTOGRAPHY_STANDARDS,
                Compliant.TRUE,
                "Encryption must follow company cryptographic standards.",
                1
        );

        ProcessCompliant pc2 = new ProcessCompliant(
                "pc-001",
                StandardGuideline.CRYPTOGRAPHY_STANDARDS,
                Compliant.TRUE,
                "Encryption must follow company cryptographic standards.",
                1
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
                "Encryption must follow company cryptographic standards.",
                1
        );

        String toStringResult = processCompliant.toString();
        assertTrue(toStringResult.contains("pc-001"));
        assertTrue(toStringResult.contains("CRYPTOGRAPHY_STANDARDS"));
        assertTrue(toStringResult.contains("TRUE"));
        assertTrue(toStringResult.contains("Encryption must follow company cryptographic standards."));
    }
}
