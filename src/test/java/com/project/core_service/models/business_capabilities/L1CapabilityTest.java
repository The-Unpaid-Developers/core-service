package com.project.core_service.models.business_capabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class L1CapabilityTest {

    @Test
    void shouldHaveExpectedNumberOfEnumValues() {
        L1Capability[] values = L1Capability.values();
        assertThat(values).hasSize(11);
    }

    @Test
    void shouldContainAllExpectedEnumValues() {
        assertThat(L1Capability.values()).containsExactlyInAnyOrder(
            L1Capability.POLICY_MANAGEMENT,
            L1Capability.CLAIMS_MANAGEMENT,
            L1Capability.CUSTOMER_MANAGEMENT,
            L1Capability.UNDERWRITING,
            L1Capability.PRODUCT_DEVELOPMENT,
            L1Capability.DISTRIBUTION_AND_SALES,
            L1Capability.FINANCE_AND_ACCOUNTING,
            L1Capability.RISK_MANAGEMENT,
            L1Capability.COMPLIANCE_AND_REGULATORY,
            L1Capability.IT_AND_OPERATIONS,
            L1Capability.UNKNOWN
        );
    }

    @ParameterizedTest
    @EnumSource(L1Capability.class)
    void shouldBeAccessibleByValueOf(L1Capability capability) {
        L1Capability result = L1Capability.valueOf(capability.name());
        assertThat(result).isEqualTo(capability);
    }

    @Test
    void valueOf_PolicyManagement_Success() {
        L1Capability capability = L1Capability.valueOf("POLICY_MANAGEMENT");
        assertThat(capability).isEqualTo(L1Capability.POLICY_MANAGEMENT);
    }

    @Test
    void valueOf_ClaimsManagement_Success() {
        L1Capability capability = L1Capability.valueOf("CLAIMS_MANAGEMENT");
        assertThat(capability).isEqualTo(L1Capability.CLAIMS_MANAGEMENT);
    }

    @Test
    void valueOf_CustomerManagement_Success() {
        L1Capability capability = L1Capability.valueOf("CUSTOMER_MANAGEMENT");
        assertThat(capability).isEqualTo(L1Capability.CUSTOMER_MANAGEMENT);
    }

    @Test
    void valueOf_Underwriting_Success() {
        L1Capability capability = L1Capability.valueOf("UNDERWRITING");
        assertThat(capability).isEqualTo(L1Capability.UNDERWRITING);
    }

    @Test
    void valueOf_ProductDevelopment_Success() {
        L1Capability capability = L1Capability.valueOf("PRODUCT_DEVELOPMENT");
        assertThat(capability).isEqualTo(L1Capability.PRODUCT_DEVELOPMENT);
    }

    @Test
    void valueOf_DistributionAndSales_Success() {
        L1Capability capability = L1Capability.valueOf("DISTRIBUTION_AND_SALES");
        assertThat(capability).isEqualTo(L1Capability.DISTRIBUTION_AND_SALES);
    }

    @Test
    void valueOf_FinanceAndAccounting_Success() {
        L1Capability capability = L1Capability.valueOf("FINANCE_AND_ACCOUNTING");
        assertThat(capability).isEqualTo(L1Capability.FINANCE_AND_ACCOUNTING);
    }

    @Test
    void valueOf_RiskManagement_Success() {
        L1Capability capability = L1Capability.valueOf("RISK_MANAGEMENT");
        assertThat(capability).isEqualTo(L1Capability.RISK_MANAGEMENT);
    }

    @Test
    void valueOf_ComplianceAndRegulatory_Success() {
        L1Capability capability = L1Capability.valueOf("COMPLIANCE_AND_REGULATORY");
        assertThat(capability).isEqualTo(L1Capability.COMPLIANCE_AND_REGULATORY);
    }

    @Test
    void valueOf_ItAndOperations_Success() {
        L1Capability capability = L1Capability.valueOf("IT_AND_OPERATIONS");
        assertThat(capability).isEqualTo(L1Capability.IT_AND_OPERATIONS);
    }

    @Test
    void valueOf_Unknown_Success() {
        L1Capability capability = L1Capability.valueOf("UNKNOWN");
        assertThat(capability).isEqualTo(L1Capability.UNKNOWN);
    }

    @Test
    void valueOf_InvalidValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> L1Capability.valueOf("INVALID_CAPABILITY"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valueOf_NullValue_ThrowsNullPointerException() {
        assertThatThrownBy(() -> L1Capability.valueOf(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void valueOf_LowercaseValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> L1Capability.valueOf("policy_management"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHaveCorrectOrdinalValues() {
        assertThat(L1Capability.POLICY_MANAGEMENT.ordinal()).isEqualTo(0);
        assertThat(L1Capability.CLAIMS_MANAGEMENT.ordinal()).isEqualTo(1);
        assertThat(L1Capability.CUSTOMER_MANAGEMENT.ordinal()).isEqualTo(2);
        assertThat(L1Capability.UNDERWRITING.ordinal()).isEqualTo(3);
        assertThat(L1Capability.PRODUCT_DEVELOPMENT.ordinal()).isEqualTo(4);
        assertThat(L1Capability.DISTRIBUTION_AND_SALES.ordinal()).isEqualTo(5);
        assertThat(L1Capability.FINANCE_AND_ACCOUNTING.ordinal()).isEqualTo(6);
        assertThat(L1Capability.RISK_MANAGEMENT.ordinal()).isEqualTo(7);
        assertThat(L1Capability.COMPLIANCE_AND_REGULATORY.ordinal()).isEqualTo(8);
        assertThat(L1Capability.IT_AND_OPERATIONS.ordinal()).isEqualTo(9);
        assertThat(L1Capability.UNKNOWN.ordinal()).isEqualTo(10);
    }

    @Test
    void shouldHaveCorrectNameValues() {
        assertThat(L1Capability.POLICY_MANAGEMENT.name()).isEqualTo("POLICY_MANAGEMENT");
        assertThat(L1Capability.CLAIMS_MANAGEMENT.name()).isEqualTo("CLAIMS_MANAGEMENT");
        assertThat(L1Capability.CUSTOMER_MANAGEMENT.name()).isEqualTo("CUSTOMER_MANAGEMENT");
        assertThat(L1Capability.UNDERWRITING.name()).isEqualTo("UNDERWRITING");
        assertThat(L1Capability.PRODUCT_DEVELOPMENT.name()).isEqualTo("PRODUCT_DEVELOPMENT");
        assertThat(L1Capability.DISTRIBUTION_AND_SALES.name()).isEqualTo("DISTRIBUTION_AND_SALES");
        assertThat(L1Capability.FINANCE_AND_ACCOUNTING.name()).isEqualTo("FINANCE_AND_ACCOUNTING");
        assertThat(L1Capability.RISK_MANAGEMENT.name()).isEqualTo("RISK_MANAGEMENT");
        assertThat(L1Capability.COMPLIANCE_AND_REGULATORY.name()).isEqualTo("COMPLIANCE_AND_REGULATORY");
        assertThat(L1Capability.IT_AND_OPERATIONS.name()).isEqualTo("IT_AND_OPERATIONS");
        assertThat(L1Capability.UNKNOWN.name()).isEqualTo("UNKNOWN");
    }

    @Test
    void shouldBeEqualWhenSameEnumValue() {
        L1Capability cap1 = L1Capability.POLICY_MANAGEMENT;
        L1Capability cap2 = L1Capability.POLICY_MANAGEMENT;

        assertThat(cap1).isEqualTo(cap2);
        assertThat(cap1).isSameAs(cap2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentEnumValues() {
        L1Capability cap1 = L1Capability.POLICY_MANAGEMENT;
        L1Capability cap2 = L1Capability.CLAIMS_MANAGEMENT;

        assertThat(cap1).isNotEqualTo(cap2);
    }

    @Test
    void unknownShouldBeLastValue() {
        L1Capability[] values = L1Capability.values();
        assertThat(values[values.length - 1]).isEqualTo(L1Capability.UNKNOWN);
    }
}
