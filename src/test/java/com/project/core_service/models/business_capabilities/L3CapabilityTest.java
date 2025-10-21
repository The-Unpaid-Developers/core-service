package com.project.core_service.models.business_capabilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class L3CapabilityTest {

    @Test
    void shouldHaveExpectedNumberOfEnumValues() {
        L3Capability[] values = L3Capability.values();
        assertThat(values).hasSize(78);
    }

    @Test
    void shouldContainAllExpectedEnumValues() {
        assertThat(L3Capability.values()).containsExactlyInAnyOrder(
            L3Capability.POLICY_ISSUANCE,
            L3Capability.POLICY_RENEWAL,
            L3Capability.POLICY_MODIFICATION,
            L3Capability.POLICY_CANCELLATION,
            L3Capability.PREMIUM_BILLING,
            L3Capability.POLICY_DOCUMENT_MANAGEMENT,
            L3Capability.POLICY_INQUIRY_MANAGEMENT,
            L3Capability.FIRST_NOTICE_OF_LOSS,
            L3Capability.CLAIMS_INVESTIGATION,
            L3Capability.CLAIMS_ADJUDICATION,
            L3Capability.CLAIMS_PAYMENT,
            L3Capability.CLAIMS_DOCUMENT_MANAGEMENT,
            L3Capability.FRAUD_DETECTION,
            L3Capability.SUBROGATION_MANAGEMENT,
            L3Capability.CUSTOMER_REGISTRATION,
            L3Capability.KYC_VERIFICATION,
            L3Capability.CUSTOMER_PROFILE_SETUP,
            L3Capability.CUSTOMER_INQUIRY_HANDLING,
            L3Capability.COMPLAINT_MANAGEMENT,
            L3Capability.CUSTOMER_COMMUNICATIONS,
            L3Capability.LOYALTY_PROGRAM_MANAGEMENT,
            L3Capability.CUSTOMER_RELATIONSHIP_MANAGEMENT,
            L3Capability.RISK_EVALUATION,
            L3Capability.RISK_SCORING,
            L3Capability.MEDICAL_UNDERWRITING,
            L3Capability.PREMIUM_CALCULATION,
            L3Capability.RATE_MANAGEMENT,
            L3Capability.QUOTE_GENERATION,
            L3Capability.APPROVAL_WORKFLOW,
            L3Capability.REINSURANCE_PLACEMENT,
            L3Capability.PRODUCT_DEFINITION,
            L3Capability.PRODUCT_TESTING,
            L3Capability.PRODUCT_DOCUMENTATION,
            L3Capability.PRODUCT_LAUNCH,
            L3Capability.PRODUCT_PERFORMANCE_MONITORING,
            L3Capability.PRODUCT_RETIREMENT,
            L3Capability.AGENT_MANAGEMENT,
            L3Capability.DIGITAL_CHANNEL_MANAGEMENT,
            L3Capability.PARTNER_MANAGEMENT,
            L3Capability.LEAD_MANAGEMENT,
            L3Capability.QUOTE_MANAGEMENT,
            L3Capability.COMMISSION_MANAGEMENT,
            L3Capability.SALES_ANALYTICS,
            L3Capability.SALES_FORECASTING,
            L3Capability.GENERAL_LEDGER_MANAGEMENT,
            L3Capability.ACCOUNTS_PAYABLE,
            L3Capability.ACCOUNTS_RECEIVABLE,
            L3Capability.REGULATORY_REPORTING,
            L3Capability.MANAGEMENT_REPORTING,
            L3Capability.STATUTORY_REPORTING,
            L3Capability.PORTFOLIO_MANAGEMENT,
            L3Capability.ASSET_ALLOCATION,
            L3Capability.RISK_IDENTIFICATION,
            L3Capability.RISK_MONITORING,
            L3Capability.RISK_MITIGATION,
            L3Capability.RESERVE_CALCULATION,
            L3Capability.LOSS_FORECASTING,
            L3Capability.PRICING_ANALYTICS,
            L3Capability.LICENSING_MANAGEMENT,
            L3Capability.REGULATORY_CHANGE_MANAGEMENT,
            L3Capability.COMPLIANCE_MONITORING,
            L3Capability.INTERNAL_AUDIT,
            L3Capability.POLICY_MANAGEMENT,
            L3Capability.REGULATORY_FILING,
            L3Capability.COMPLIANCE_REPORTING,
            L3Capability.SYSTEM_DEVELOPMENT,
            L3Capability.SYSTEM_INTEGRATION,
            L3Capability.SYSTEM_MAINTENANCE,
            L3Capability.DATA_CENTER_OPERATIONS,
            L3Capability.NETWORK_MANAGEMENT,
            L3Capability.CLOUD_SERVICES_MANAGEMENT,
            L3Capability.DATA_GOVERNANCE,
            L3Capability.DATA_QUALITY_MANAGEMENT,
            L3Capability.DATA_ANALYTICS,
            L3Capability.SECURITY_OPERATIONS,
            L3Capability.ACCESS_MANAGEMENT,
            L3Capability.INCIDENT_RESPONSE,
            L3Capability.UNKNOWN
        );
    }

    @ParameterizedTest
    @EnumSource(L3Capability.class)
    void shouldBeAccessibleByValueOf(L3Capability capability) {
        L3Capability result = L3Capability.valueOf(capability.name());
        assertThat(result).isEqualTo(capability);
    }

    @Test
    void valueOf_PolicyIssuance_Success() {
        L3Capability capability = L3Capability.valueOf("POLICY_ISSUANCE");
        assertThat(capability).isEqualTo(L3Capability.POLICY_ISSUANCE);
    }

    @Test
    void valueOf_ClaimsInvestigation_Success() {
        L3Capability capability = L3Capability.valueOf("CLAIMS_INVESTIGATION");
        assertThat(capability).isEqualTo(L3Capability.CLAIMS_INVESTIGATION);
    }

    @Test
    void valueOf_CustomerRegistration_Success() {
        L3Capability capability = L3Capability.valueOf("CUSTOMER_REGISTRATION");
        assertThat(capability).isEqualTo(L3Capability.CUSTOMER_REGISTRATION);
    }

    @Test
    void valueOf_KycVerification_Success() {
        L3Capability capability = L3Capability.valueOf("KYC_VERIFICATION");
        assertThat(capability).isEqualTo(L3Capability.KYC_VERIFICATION);
    }

    @Test
    void valueOf_RiskEvaluation_Success() {
        L3Capability capability = L3Capability.valueOf("RISK_EVALUATION");
        assertThat(capability).isEqualTo(L3Capability.RISK_EVALUATION);
    }

    @Test
    void valueOf_FraudDetection_Success() {
        L3Capability capability = L3Capability.valueOf("FRAUD_DETECTION");
        assertThat(capability).isEqualTo(L3Capability.FRAUD_DETECTION);
    }

    @Test
    void valueOf_ProductDefinition_Success() {
        L3Capability capability = L3Capability.valueOf("PRODUCT_DEFINITION");
        assertThat(capability).isEqualTo(L3Capability.PRODUCT_DEFINITION);
    }

    @Test
    void valueOf_AgentManagement_Success() {
        L3Capability capability = L3Capability.valueOf("AGENT_MANAGEMENT");
        assertThat(capability).isEqualTo(L3Capability.AGENT_MANAGEMENT);
    }

    @Test
    void valueOf_SalesAnalytics_Success() {
        L3Capability capability = L3Capability.valueOf("SALES_ANALYTICS");
        assertThat(capability).isEqualTo(L3Capability.SALES_ANALYTICS);
    }

    @Test
    void valueOf_GeneralLedgerManagement_Success() {
        L3Capability capability = L3Capability.valueOf("GENERAL_LEDGER_MANAGEMENT");
        assertThat(capability).isEqualTo(L3Capability.GENERAL_LEDGER_MANAGEMENT);
    }

    @Test
    void valueOf_RegulatoryReporting_Success() {
        L3Capability capability = L3Capability.valueOf("REGULATORY_REPORTING");
        assertThat(capability).isEqualTo(L3Capability.REGULATORY_REPORTING);
    }

    @Test
    void valueOf_PortfolioManagement_Success() {
        L3Capability capability = L3Capability.valueOf("PORTFOLIO_MANAGEMENT");
        assertThat(capability).isEqualTo(L3Capability.PORTFOLIO_MANAGEMENT);
    }

    @Test
    void valueOf_ComplianceMonitoring_Success() {
        L3Capability capability = L3Capability.valueOf("COMPLIANCE_MONITORING");
        assertThat(capability).isEqualTo(L3Capability.COMPLIANCE_MONITORING);
    }

    @Test
    void valueOf_SystemDevelopment_Success() {
        L3Capability capability = L3Capability.valueOf("SYSTEM_DEVELOPMENT");
        assertThat(capability).isEqualTo(L3Capability.SYSTEM_DEVELOPMENT);
    }

    @Test
    void valueOf_DataGovernance_Success() {
        L3Capability capability = L3Capability.valueOf("DATA_GOVERNANCE");
        assertThat(capability).isEqualTo(L3Capability.DATA_GOVERNANCE);
    }

    @Test
    void valueOf_SecurityOperations_Success() {
        L3Capability capability = L3Capability.valueOf("SECURITY_OPERATIONS");
        assertThat(capability).isEqualTo(L3Capability.SECURITY_OPERATIONS);
    }

    @Test
    void valueOf_IncidentResponse_Success() {
        L3Capability capability = L3Capability.valueOf("INCIDENT_RESPONSE");
        assertThat(capability).isEqualTo(L3Capability.INCIDENT_RESPONSE);
    }

    @Test
    void valueOf_Unknown_Success() {
        L3Capability capability = L3Capability.valueOf("UNKNOWN");
        assertThat(capability).isEqualTo(L3Capability.UNKNOWN);
    }

    @Test
    void valueOf_InvalidValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> L3Capability.valueOf("INVALID_CAPABILITY"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void valueOf_NullValue_ThrowsNullPointerException() {
        assertThatThrownBy(() -> L3Capability.valueOf(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void valueOf_LowercaseValue_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> L3Capability.valueOf("policy_issuance"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHaveCorrectOrdinalForFirstAndLastValues() {
        assertThat(L3Capability.POLICY_ISSUANCE.ordinal()).isEqualTo(0);
        assertThat(L3Capability.UNKNOWN.ordinal()).isEqualTo(77);
    }

    @Test
    void shouldHaveCorrectNameValues() {
        assertThat(L3Capability.POLICY_ISSUANCE.name()).isEqualTo("POLICY_ISSUANCE");
        assertThat(L3Capability.FRAUD_DETECTION.name()).isEqualTo("FRAUD_DETECTION");
        assertThat(L3Capability.KYC_VERIFICATION.name()).isEqualTo("KYC_VERIFICATION");
        assertThat(L3Capability.DATA_ANALYTICS.name()).isEqualTo("DATA_ANALYTICS");
        assertThat(L3Capability.INCIDENT_RESPONSE.name()).isEqualTo("INCIDENT_RESPONSE");
        assertThat(L3Capability.UNKNOWN.name()).isEqualTo("UNKNOWN");
    }

    @Test
    void shouldBeEqualWhenSameEnumValue() {
        L3Capability cap1 = L3Capability.POLICY_ISSUANCE;
        L3Capability cap2 = L3Capability.POLICY_ISSUANCE;

        assertThat(cap1).isEqualTo(cap2);
        assertThat(cap1).isSameAs(cap2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentEnumValues() {
        L3Capability cap1 = L3Capability.POLICY_ISSUANCE;
        L3Capability cap2 = L3Capability.CLAIMS_INVESTIGATION;

        assertThat(cap1).isNotEqualTo(cap2);
    }

    @Test
    void unknownShouldBeLastValue() {
        L3Capability[] values = L3Capability.values();
        assertThat(values[values.length - 1]).isEqualTo(L3Capability.UNKNOWN);
    }

    @Test
    void shouldHaveUniqueValues() {
        L3Capability[] values = L3Capability.values();
        assertThat(values).doesNotHaveDuplicates();
    }

    @Test
    void shouldContainPolicyRelatedCapabilities() {
        assertThat(L3Capability.values()).contains(
            L3Capability.POLICY_ISSUANCE,
            L3Capability.POLICY_RENEWAL,
            L3Capability.POLICY_MODIFICATION,
            L3Capability.POLICY_CANCELLATION,
            L3Capability.POLICY_DOCUMENT_MANAGEMENT,
            L3Capability.POLICY_INQUIRY_MANAGEMENT,
            L3Capability.POLICY_MANAGEMENT
        );
    }

    @Test
    void shouldContainClaimsRelatedCapabilities() {
        assertThat(L3Capability.values()).contains(
            L3Capability.FIRST_NOTICE_OF_LOSS,
            L3Capability.CLAIMS_INVESTIGATION,
            L3Capability.CLAIMS_ADJUDICATION,
            L3Capability.CLAIMS_PAYMENT,
            L3Capability.CLAIMS_DOCUMENT_MANAGEMENT
        );
    }

    @Test
    void shouldContainRiskRelatedCapabilities() {
        assertThat(L3Capability.values()).contains(
            L3Capability.RISK_EVALUATION,
            L3Capability.RISK_SCORING,
            L3Capability.RISK_IDENTIFICATION,
            L3Capability.RISK_MONITORING,
            L3Capability.RISK_MITIGATION
        );
    }

    @Test
    void shouldContainSecurityRelatedCapabilities() {
        assertThat(L3Capability.values()).contains(
            L3Capability.SECURITY_OPERATIONS,
            L3Capability.ACCESS_MANAGEMENT,
            L3Capability.INCIDENT_RESPONSE
        );
    }
}
