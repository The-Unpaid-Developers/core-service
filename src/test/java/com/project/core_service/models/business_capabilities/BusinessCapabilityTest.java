package com.project.core_service.models.business_capabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BusinessCapabilityTest {
    @Test
    void shouldCreateBusinessCapabilitySuccessfully() {
        BusinessCapability capability = new BusinessCapability(
                "bc-001",
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                "Handles UNKNOWN operations"
        );

        assertThat(capability.getId()).isEqualTo("bc-001");
        assertThat(capability.getL1Capability()).isEqualTo("UNKNOWN");
        assertThat(capability.getL2Capability()).isEqualTo("UNKNOWN");
        assertThat(capability.getL3Capability()).isEqualTo("UNKNOWN");
        assertThat(capability.getRemarks()).isEqualTo("Handles UNKNOWN operations");
    }

    @Test
    void testBusinessCapabilityBuilder() {
        BusinessCapability bc = BusinessCapability.builder()
                .id("cap-123")
                .l1Capability("L1")
                .l2Capability("L2")
                .l3Capability("L3")
                .remarks("Core payment capability")
                .build();

        assertEquals("cap-123", bc.getId());
        assertEquals("L1", bc.getL1Capability());
        assertEquals("L2", bc.getL2Capability());
        assertEquals("L3", bc.getL3Capability());
    }

    @Test
    void shouldRespectEqualsAndHashCode() {
        BusinessCapability a = new BusinessCapability(
                "bc-001",
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                "Handles UNKNOWN operations"
                );

        BusinessCapability b = new BusinessCapability(
                "bc-001",
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN",
                "Handles UNKNOWN operations"
                );

        assertThat(a)
                .isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

}