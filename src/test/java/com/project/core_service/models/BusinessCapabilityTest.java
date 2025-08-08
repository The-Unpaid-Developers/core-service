package com.project.core_service.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.business_capabilities.L1Capability;
import com.project.core_service.models.business_capabilities.L2Capability;
import com.project.core_service.models.business_capabilities.L3Capability;

class BusinessCapabilityTest {

    @Test
    void shouldCreateBusinessCapabilitySuccessfully() {
        BusinessCapability capability = new BusinessCapability(
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN);
        capability.setId("bc-001");
        capability.setL1Capability(L1Capability.UNKNOWN);
        capability.setL2Capability(L2Capability.UNKNOWN);
        capability.setL3Capability(L3Capability.UNKNOWN);
        capability.setRemarks("Handles UNKNOWN operations");
        capability.setVersion(1);

        assertThat(capability.getId()).isEqualTo("bc-001");
        assertThat(capability.getL1Capability()).isEqualTo(L1Capability.UNKNOWN);
        assertThat(capability.getL2Capability()).isEqualTo(L2Capability.UNKNOWN);
        assertThat(capability.getL3Capability()).isEqualTo(L3Capability.UNKNOWN);
        assertThat(capability.getRemarks()).isEqualTo("Handles UNKNOWN operations");
        assertThat(capability.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenSettingNullForNonNullFields() {
        BusinessCapability capability = new BusinessCapability(
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN);

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
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN);
        a.setId("same-id");
        a.setL1Capability(L1Capability.UNKNOWN);
        a.setL2Capability(L2Capability.UNKNOWN);
        a.setL3Capability(L3Capability.UNKNOWN);
        a.setVersion(1);

        BusinessCapability b = new BusinessCapability(
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN);
        b.setId("same-id");
        b.setL1Capability(L1Capability.UNKNOWN);
        b.setL2Capability(L2Capability.UNKNOWN);
        b.setL3Capability(L3Capability.UNKNOWN);
        b.setVersion(1);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toStringShouldContainMeaningfulInfo() {
        BusinessCapability capability = new BusinessCapability(
                L1Capability.UNKNOWN,
                L2Capability.UNKNOWN,
                L3Capability.UNKNOWN);
        capability.setId("bc-002");
        capability.setL1Capability(L1Capability.UNKNOWN);
        capability.setL2Capability(L2Capability.UNKNOWN);
        capability.setL3Capability(L3Capability.UNKNOWN);

        String output = capability.toString();
        assertThat(output).contains("bc-002", "UNKNOWN", "UNKNOWN", "UNKNOWN");
    }
}
