package com.project.core_service.models.technology_component;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TechnologyComponentTest {
    @Test
    void testBuilderCreatesTechnologyComponent() {
        TechnologyComponent component = TechnologyComponent.builder()
                .id("tech-001")
                .productName("PostgreSQL")
                .productVersion("15.3")
                .usage(Usage.PREREQUISITE_INSTALLATION)
                .version(1)
                .build();

        assertNotNull(component);
        assertEquals("tech-001", component.getId());
        assertEquals("PostgreSQL", component.getProductName());
        assertEquals("15.3", component.getProductVersion());
        assertEquals(Usage.PREREQUISITE_INSTALLATION, component.getUsage());
        assertEquals(1, component.getVersion());
    }
    @Test
    void testBuilderWithNullNonNullFieldThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            TechnologyComponent.builder()
                    .id("tech-003")
                    .productName(null) // productName is @NonNull
                    .productVersion("1.0")
                    .usage(Usage.PREREQUISITE_INSTALLATION)
                    .version(3)
                    .build();
        });
    }
    @Test
    void testConstructorAndGetters() {
        TechnologyComponent tc = new TechnologyComponent(
                "tech-001",
                "PostgreSQL",
                "14.1",
                Usage.INFRASTRUCTURE
        );

        assertEquals("tech-001", tc.getId());
        assertEquals("PostgreSQL", tc.getProductName());
        assertEquals("14.1", tc.getProductVersion());
        assertEquals(Usage.INFRASTRUCTURE, tc.getUsage());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        assertThrows(NullPointerException.class, () -> new TechnologyComponent(
                "tech-002",
                null,
                "1.0",
                Usage.BASIC_INSTALLATION
        ));

        assertThrows(NullPointerException.class, () -> new TechnologyComponent(
                "tech-003",
                "Redis",
                null,
                Usage.BASIC_INSTALLATION
        ));

        assertThrows(NullPointerException.class, () -> new TechnologyComponent(
                "tech-004",
                "Redis",
                "6.2",
                null
        ));
    }

    @Test
    void testEqualsAndHashCode() {
        TechnologyComponent a = new TechnologyComponent(
                "tech-005",
                "Kafka",
                "3.0",
                Usage.INFRASTRUCTURE
        );

        TechnologyComponent b = new TechnologyComponent(
                "tech-005",
                "Kafka",
                "3.0",
                Usage.INFRASTRUCTURE
        );

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testToStringContainsKeyFields() {
        TechnologyComponent tc = new TechnologyComponent(
                "tech-006",
                "MongoDB",
                "5.0",
                Usage.INFRASTRUCTURE
        );

        String toString = tc.toString();
        assertTrue(toString.contains("tech-006"));
        assertTrue(toString.contains("MongoDB"));
        assertTrue(toString.contains("5.0"));
        assertTrue(toString.contains("INFRASTRUCTURE"));
    }
}
