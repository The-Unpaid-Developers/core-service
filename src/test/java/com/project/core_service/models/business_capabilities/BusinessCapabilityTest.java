package com.project.core_service.models.business_capabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

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
    void shouldThrowExceptionWhenSettingNullForNonNullFields() {
        BusinessCapability capability = new BusinessCapability(
                "bc-001",
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN,
                "Handles UNKNOWN operations"
                );

        assertThatThrownBy(() -> capability.setL1Capability(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> capability.setL2Capability(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> capability.setL3Capability(null))
                .isInstanceOf(NullPointerException.class);
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

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toStringShouldContainMeaningfulInfo() {
        BusinessCapability capability = new BusinessCapability(
                "bc-002",
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN,
                ""
                );

        String output = capability.toString();
        assertThat(output).contains("bc-002", "UNKNOWN", "UNKNOWN", "UNKNOWN");
    }
}
