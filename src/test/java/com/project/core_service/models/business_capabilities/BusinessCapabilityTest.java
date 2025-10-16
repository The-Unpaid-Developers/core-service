package com.project.core_service.models.business_capabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

public class BusinessCapabilityTest {
    @Test
    void shouldCreateBusinessCapabilitySuccessfully() {
        BusinessCapability capability = new BusinessCapability(
                "bc-001",
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN,
                "Handles UNKNOWN operations"
        );

        assertThat(capability.getId()).isEqualTo("bc-001");
        assertThat(capability.getL1Capability()).isEqualTo(L1Capability.UNKNOWN);
        assertThat(capability.getL2Capability()).isEqualTo(L2Capability.UNKNOWN);
        assertThat(capability.getL3Capability()).isEqualTo(L3Capability.UNKNOWN);
        assertThat(capability.getRemarks()).isEqualTo("Handles UNKNOWN operations");
    }

    @Test
    void testBusinessCapabilityBuilder() {
        L1Capability l1 = L1Capability.UNKNOWN;
        L2Capability l2 = L2Capability.UNKNOWN;
        L3Capability l3 = L3Capability.UNKNOWN;
        BusinessCapability bc = BusinessCapability.builder()
                .id("cap-123")
                .l1Capability(l1)
                .l2Capability(l2)
                .l3Capability(l3)
                .remarks("Core payment capability")
                .build();

        assertEquals("cap-123", bc.getId());
        assertEquals(l1, bc.getL1Capability());
        assertEquals(l2, bc.getL2Capability());
        assertEquals(l3, bc.getL3Capability());
    }

    @Test
    void shouldRespectEqualsAndHashCode() {
        BusinessCapability a = new BusinessCapability(
                "bc-001",
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN,
                "Handles UNKNOWN operations"
                );

        BusinessCapability b = new BusinessCapability(
                "bc-001",
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN,
                "Handles UNKNOWN operations"
                );

        assertThat(a)
                .isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

}