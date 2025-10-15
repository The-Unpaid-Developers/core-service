package com.project.core_service.models.business_capabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

public class BusinessCapabilityTest {
    private final List<Capability> capabilities = List.of(
            new Capability("Capability1", CapabilityType.L1),
            new Capability("Capability2", CapabilityType.L2),
            new Capability("Capability3", CapabilityType.L3));

    @Test
    void shouldCreateBusinessCapabilitySuccessfully() {
        BusinessCapability capability = new BusinessCapability(
                "bc-001",
                "Handles UNKNOWN operations",
                capabilities);

        assertThat(capability.getId()).isEqualTo("bc-001");
        assertThat(capability.getCapabilities()).isEqualTo(capabilities);
        assertThat(capability.getRemarks()).isEqualTo("Handles UNKNOWN operations");
    }

    @Test
    void testBusinessCapabilityBuilder() {
        BusinessCapability bc = BusinessCapability.builder()
                .id("cap-123")
                .capabilities(capabilities)
                .remarks("Core payment capability")
                .build();

        assertEquals("cap-123", bc.getId());
        assertEquals(capabilities, bc.getCapabilities());
    }

    @Test
    void shouldRespectEqualsAndHashCode() {
        BusinessCapability a = new BusinessCapability(
                "bc-001",
                "Handles UNKNOWN operations",
                capabilities);

        BusinessCapability b = new BusinessCapability(
                "bc-001",
                "Handles UNKNOWN operations",
                capabilities);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

}