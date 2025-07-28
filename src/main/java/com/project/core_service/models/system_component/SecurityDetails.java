package com.project.core_service.models.system_component;

import com.project.core_service.models.shared.Frequency;

import jakarta.annotation.Nonnull;

public record SecurityDetails(
                @Nonnull String authenticationMethod,

                @Nonnull String authorizationModel,

                boolean isAuditLoggingEnabled,

                @Nonnull String sensitiveDataElements,

                @Nonnull DataEncryptionAtRest dataEncryptionAtRest,

                @Nonnull String encryptionAlgorithmForDataAtRest,

                boolean hasIpWhitelisting,

                @Nonnull SSLType ssl,

                String payloadEncryptionAlgorithm,

                String digitalCertificate,

                @Nonnull String keyStore,

                @Nonnull Frequency vulnerabilityAssessmentFrequency,

                @Nonnull Frequency penetrationTestingFrequency) {

}
