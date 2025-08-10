package com.project.core_service.models.system_component;

import com.project.core_service.models.shared.Frequency;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecurityDetails {
    @Nonnull
    private String authenticationMethod;

    @Nonnull
    private String authorizationModel;

    private boolean isAuditLoggingEnabled;

    @Nonnull
    private String sensitiveDataElements;

    @Nonnull
    private DataEncryptionAtRest dataEncryptionAtRest;

    @Nonnull
    private String encryptionAlgorithmForDataAtRest;

    private boolean hasIpWhitelisting;

    @Nonnull
    private SSLType ssl;

    private String payloadEncryptionAlgorithm;
    private String digitalCertificate;

    @Nonnull
    private String keyStore;

    @Nonnull
    private Frequency vulnerabilityAssessmentFrequency;

    @Nonnull 
    private Frequency penetrationTestingFrequency;

}
