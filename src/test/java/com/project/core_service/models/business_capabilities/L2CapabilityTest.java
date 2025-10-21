package com.project.core_service.models.business_capabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class L2CapabilityTest {

    @Test
    void shouldHaveExpectedNumberOfEnumValues() {
        L2Capability[] values = L2Capability.values();
        assertThat(values).hasSize(28);
    }

    @Test
    void shouldContainAllExpectedEnumValues() {
        assertThat(L2Capability.values()).containsExactlyInAnyOrder(
            L2Capability.POLICY_ADMINISTRATION,
            L2Capability.POLICY_SERVICING,
            L2Capability.CLAIMS_PROCESSING,
            L2Capability.CLAIMS_SUPPORT,
            L2Capability.CUSTOMER_ONBOARDING,
            L2Capability.CUSTOMER_SERVICE,
            L2Capability.CUSTOMER_RETENTION,
            L2Capability.RISK_ASSESSMENT,
            L2Capability.POLICY_PRICING,
            L2Capability.DECISION_MANAGEMENT,
            L2Capability.PRODUCT_DESIGN,
            L2Capability.PRODUCT_LIFECYCLE,
            L2Capability.CHANNEL_MANAGEMENT,
            L2Capability.SALES_OPERATIONS,
            L2Capability.SALES_PERFORMANCE,
            L2Capability.FINANCIAL_ACCOUNTING,
            L2Capability.FINANCIAL_REPORTING,
            L2Capability.INVESTMENT_MANAGEMENT,
            L2Capability.ENTERPRISE_RISK,
            L2Capability.ACTUARIAL_SERVICES,
            L2Capability.REGULATORY_COMPLIANCE,
            L2Capability.POLICY_COMPLIANCE,
            L2Capability.REPORTING,
            L2Capability.APPLICATION_MANAGEMENT,
            L2Capability.INFRASTRUCTURE_MANAGEMENT,
            L2Capability.DATA_MANAGEMENT,
            L2Capability.CYBERSECURITY,
            L2Capability.UNKNOWN
        );
    }

    @ParameterizedTest
    @EnumSource(L2Capability.class)
    void shouldBeAccessibleByValueOf(L2Capability capability) {
        L2Capability result = L2Capability.valueOf(capability.name());
        assertThat(result).isEqualTo(capability);
    }

    @Test
    void valueOf_PolicyAdministration_Success() {
        L2Capability capability = L2Capability.valueOf("POLICY_ADMINISTRATION");
        assertThat(capability).isEqualTo(L2Capability.POLICY_ADMINISTRATION);
    }

    @Test
    void valueOf_ClaimsProcessing_Success() {
        L2Capability capability = L2Capability.valueOf("CLAIMS_PROCESSING");
        assertThat(capability).isEqualTo(L2Capability.CLAIMS_PROCESSING);
    }

    @Test
    void valueOf_CustomerOnboarding_Success() {
        L2Capability capability = L2Capability.valueOf("CUSTOMER_ONBOARDING");
        assertThat(capability).isEqualTo(L2Capability.CUSTOMER_ONBOARDING);
    }

    @Test
    void valueOf_RiskAssessment_Success() {
        L2Capability capability = L2Capability.valueOf("RISK_ASSESSMENT");
        assertThat(capability).isEqualTo(L2Capability.RISK_ASSESSMENT);
    }

    @Test
    void valueOf_ProductDesign_Success() {
        L2Capability capability = L2Capability.valueOf("PRODUCT_DESIGN");
        assertThat(capability).isEqualTo(L2Capability.PRODUCT_DESIGN);
    }

    @Test
    void valueOf_SalesOperations_Success() {
        L2Capability capability = L2Capability.valueOf("SALES_OPERATIONS");
        assertThat(capability).isEqualTo(L2Capability.SALES_OPERATIONS);
    }

    @Test
    void valueOf_FinancialAccounting_Success() {
        L2Capability capability = L2Capability.valueOf("FINANCIAL_ACCOUNTING");
        assertThat(capability).isEqualTo(L2Capability.FINANCIAL_ACCOUNTING);
    }

    @Test
    void valueOf_ActuarialServices_Success() {
        L2Capability capability = L2Capability.valueOf("ACTUARIAL_SERVICES");
        assertThat(capability).isEqualTo(L2Capability.ACTUARIAL_SERVICES);
    }

    @Test
    void valueOf_RegulatoryCompliance_Success() {
        L2Capability capability = L2Capability.valueOf("REGULATORY_COMPLIANCE");
        assertThat(capability).isEqualTo(L2Capability.REGULATORY_COMPLIANCE);
    }

    @Test
    void valueOf_DataManagement_Success() {
        L2Capability capability = L2Capability.valueOf("DATA_MANAGEMENT");
        assertThat(capability).isEqualTo(L2Capability.DATA_MANAGEMENT);
    }

    @Test
    void valueOf_Cybersecurity_Success() {
        L2Capability capability = L2Capability.valueOf("CYBERSECURITY");
        assertThat(capability).isEqualTo(L2Capability.CYBERSECURITY);
    }

    @Test
    void valueOf_Unknown_Success() {
        L2Capability capability = L2Capability.valueOf("UNKNOWN");
        assertThat(capability).isEqualTo(L2Capability.UNKNOWN);
    }

    @Test
    void valueOf_InvalidValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> L2Capability.valueOf("INVALID_CAPABILITY"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valueOf_NullValue_ThrowsNullPointerException() {
        assertThatThrownBy(() -> L2Capability.valueOf(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void valueOf_LowercaseValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> L2Capability.valueOf("policy_administration"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHaveCorrectOrdinalForFirstAndLastValues() {
        assertThat(L2Capability.POLICY_ADMINISTRATION.ordinal()).isEqualTo(0);
        assertThat(L2Capability.UNKNOWN.ordinal()).isEqualTo(27);
    }

    @Test
    void shouldHaveCorrectNameValues() {
        assertThat(L2Capability.POLICY_ADMINISTRATION.name()).isEqualTo("POLICY_ADMINISTRATION");
        assertThat(L2Capability.CLAIMS_PROCESSING.name()).isEqualTo("CLAIMS_PROCESSING");
        assertThat(L2Capability.CUSTOMER_ONBOARDING.name()).isEqualTo("CUSTOMER_ONBOARDING");
        assertThat(L2Capability.CYBERSECURITY.name()).isEqualTo("CYBERSECURITY");
        assertThat(L2Capability.UNKNOWN.name()).isEqualTo("UNKNOWN");
    }

    @Test
    void shouldBeEqualWhenSameEnumValue() {
        L2Capability cap1 = L2Capability.POLICY_ADMINISTRATION;
        L2Capability cap2 = L2Capability.POLICY_ADMINISTRATION;

        assertThat(cap1).isEqualTo(cap2);
        assertThat(cap1).isSameAs(cap2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentEnumValues() {
        L2Capability cap1 = L2Capability.POLICY_ADMINISTRATION;
        L2Capability cap2 = L2Capability.CLAIMS_PROCESSING;

        assertThat(cap1).isNotEqualTo(cap2);
    }

    @Test
    void unknownShouldBeLastValue() {
        L2Capability[] values = L2Capability.values();
        assertThat(values[values.length - 1]).isEqualTo(L2Capability.UNKNOWN);
    }

    @Test
    void shouldHaveUniqueValues() {
        L2Capability[] values = L2Capability.values();
        assertThat(values).doesNotHaveDuplicates();
    }
}
