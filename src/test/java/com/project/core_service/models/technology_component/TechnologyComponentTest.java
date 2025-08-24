package com.project.core_service.models.technology_component;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TechnologyComponentTest {

    @Test
    void testBuilderCreatesTechnologyComponent() {
        TechnologyComponent component = TechnologyComponent.builder()
                .id("tech-001")
                .componentName("Database Engine")
                .productName("PostgreSQL")
                .productVersion("15.3")
                .usage(Usage.PREREQUISITE_INSTALLATION)
                .build();

        assertNotNull(component);
        assertEquals("tech-001", component.getId());
        assertEquals("Database Engine", component.getComponentName());
        assertEquals("PostgreSQL", component.getProductName());
        assertEquals("15.3", component.getProductVersion());
        assertEquals(Usage.PREREQUISITE_INSTALLATION, component.getUsage());
    }

    @Test
    void testBuilderWithNullNonNullFieldThrowsException() {
        TechnologyComponent.TechnologyComponentBuilder builder = TechnologyComponent.builder()
                .id("tech-003")
                .componentName("Cache")
                .productName(null) // productName is @NonNull
                .productVersion("1.0")
                .usage(Usage.PREREQUISITE_INSTALLATION);

        assertThrows(NullPointerException.class, () -> builder.build());
    }

    @Test
    void testConstructorAndGetters() {
        TechnologyComponent tc = new TechnologyComponent(
                "tech-001",
                "Database Engine",
                "PostgreSQL",
                "14.1",
                Usage.INFRASTRUCTURE);

        assertEquals("tech-001", tc.getId());
        assertEquals("Database Engine", tc.getComponentName());
        assertEquals("PostgreSQL", tc.getProductName());
        assertEquals("14.1", tc.getProductVersion());
        assertEquals(Usage.INFRASTRUCTURE, tc.getUsage());
    }

    @Test
    void shouldAllowNullComponentName() {
        TechnologyComponent tc = new TechnologyComponent(
                "tech-002",
                null, // allowed
                "Redis",
                "6.2",
                Usage.BASIC_INSTALLATION);

        assertNull(tc.getComponentName());
        assertEquals("Redis", tc.getProductName());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        assertThrows(NullPointerException.class, () -> new TechnologyComponent(
                "tech-002",
                "Cache",
                null,
                "1.0",
                Usage.BASIC_INSTALLATION));

        assertThrows(NullPointerException.class, () -> new TechnologyComponent(
                "tech-003",
                "Cache",
                "Redis",
                null,
                Usage.BASIC_INSTALLATION));

        assertThrows(NullPointerException.class, () -> new TechnologyComponent(
                "tech-004",
                "Cache",
                "Redis",
                "6.2",
                null));
    }

    @Test
    void testEqualsAndHashCode() {
        TechnologyComponent a = new TechnologyComponent(
                "tech-005",
                "Messaging",
                "Kafka",
                "3.0",
                Usage.INFRASTRUCTURE);

        TechnologyComponent b = new TechnologyComponent(
                "tech-005",
                "Messaging",
                "Kafka",
                "3.0",
                Usage.INFRASTRUCTURE);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testToStringContainsKeyFields() {
        TechnologyComponent tc = new TechnologyComponent(
                "tech-006",
                "Database",
                "MongoDB",
                "5.0",
                Usage.INFRASTRUCTURE);

        String toString = tc.toString();
        assertTrue(toString.contains("tech-006"));
        assertTrue(toString.contains("Database"));
        assertTrue(toString.contains("MongoDB"));
        assertTrue(toString.contains("5.0"));
        assertTrue(toString.contains("INFRASTRUCTURE"));
    }
}
