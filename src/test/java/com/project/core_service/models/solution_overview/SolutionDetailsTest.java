package com.project.core_service.models.solution_overview;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class SolutionDetailsTest {

    @Test
    void testConstructorAndGetters() {
        SolutionDetails details = new SolutionDetails(
                "Awesome Solution",
                "Project Phoenix",
                "AWG123",
                "Alice Architect",
                "Bob PM",
                "Partner1"
        );

        assertEquals("Awesome Solution", details.getSolutionName());
        assertEquals("Project Phoenix", details.getProjectName());
        assertEquals("AWG123", details.getSolutionReviewCode());
        assertEquals("Alice Architect", details.getSolutionArchitectName());
        assertEquals("Bob PM", details.getDeliveryProjectManagerName());
        assertEquals("Partner1", details.getItBusinessPartner());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {

        assertThrows(NullPointerException.class, () -> new SolutionDetails(
                null,
                "Project Phoenix",
                "AWG123",
                "Alice Architect",
                "Bob PM",
                "Partner1"
        ));


        assertThrows(NullPointerException.class, () -> new SolutionDetails(
                "Solution",
                null,
                "AWG123",
                "Alice Architect",
                "Bob PM",
                "Partner1"
        ));

        assertThrows(NullPointerException.class, () -> new SolutionDetails(
                "Solution",
                "Project Phoenix",
                null,
                "Alice Architect",
                "Bob PM",
                "Partner1"
        ));

        assertThrows(NullPointerException.class, () -> new SolutionDetails(
                "Solution",
                "Project Phoenix",
                "AWG123",
                null,
                "Bob PM",
                "Partner1"
        ));

        assertThrows(NullPointerException.class, () -> new SolutionDetails(
                "Solution",
                "Project Phoenix",
                "AWG123",
                "Alice Architect",
                null,
                "Partner1"
        ));

        assertThrows(NullPointerException.class, () -> new SolutionDetails(
                "Solution",
                "Project Phoenix",
                "AWG123",
                "Alice Architect",
                "Bob PM",
                null
        ));
    }

    @Test
    void testEqualsAndHashCode() {
        SolutionDetails sd1 = new SolutionDetails(
                "SolutionX",
                "Project Delta",
                "AWG456",
                "Eve Architect",
                "Carol PM",
                "PartnerA"
        );

        SolutionDetails sd2 = new SolutionDetails(
                "SolutionX",
                "Project Delta",
                "AWG456",
                "Eve Architect",
                "Carol PM",
                "PartnerA"
        );

        assertEquals(sd1, sd2);
        assertEquals(sd1.hashCode(), sd2.hashCode());
    }

    @Test
    void testToStringContainsKeyFields() {
        SolutionDetails details = new SolutionDetails(
                "SolutionY",
                "Project Gamma",
                "AWG789",
                "Dave Architect",
                "Frank PM",
                "PartnerX"
        );

        String toString = details.toString();
        assertTrue(toString.contains("SolutionY"));
        assertTrue(toString.contains("Project Gamma"));
        assertTrue(toString.contains("AWG789"));
        assertTrue(toString.contains("Dave Architect"));
        assertTrue(toString.contains("Frank PM"));
        assertTrue(toString.contains("PartnerX"));
    }
}
