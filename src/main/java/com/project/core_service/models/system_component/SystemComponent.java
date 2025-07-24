package com.project.core_service.models.system_component;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import jakarta.annotation.Nonnull;
import lombok.Data;

@Document
@Data
public class SystemComponent {
    @Id
    private String id;

    @NonNull
    private String name;

    @NonNull
    private ComponentStatus status;

    @NonNull
    private ComponentRole role;

    @NonNull
    private Location hostedOn;

    @NonNull
    private HostingRegion hostingRegion;

    @NonNull
    private SolutionType solutionType;

    @NonNull
    private LanguageFramework languageFramework;

    // true if owned by us false if owned by vendors
    private boolean isOwned;

    private boolean isCicdUsed;

    @NonNull
    private CustomizationLevel customizationLevel;

    @NonNull
    private UpgradeStrategy upgradeStrategy;

    @NonNull
    private UpgradeFrequency upgradeFrequency;

    // true if is subscription, false if it's one-time purchase
    private boolean isSubscription;

    private boolean isInternetFacing;

    @NonNull
    private AvailabilityRequirement availabilityRequirement;

    // Number of seconds
    private int latencyRequirement;

    // Requests per second
    private int throughputRequirement;

    @NonNull
    private ScalabilityMethod scalabilityMethod;

    @NonNull
    private BackupSite backupSite;

    @NonNull
    private String authenticationMethod;

    @NonNull
    private String authorizationModel;

    private boolean isAuditLoggingEnabled;

    @Nonnull
    private String sensitiveDataElements;

    @NonNull
    private DataEncryptionAtRest dataEncryptionAtRest;

    @NonNull
    private String encryptionAlgorithmForDataAtRest;

    private boolean hasIpWhitelisting;

    @NonNull
    private SSLType ssl;

    @NonNull
    private String payloadEncryptionAlgorithm;

    @NonNull
    private String digitalCertificate;

    @NonNull
    private String keyStore;

    @NonNull
    private String vulnerabilityAssessmentFrequency;

    @NonNull
    private String penetrationTestingFrequency;

    @NonNull
    private String externalSystem;
}
