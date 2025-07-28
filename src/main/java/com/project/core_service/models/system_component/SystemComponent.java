package com.project.core_service.models.system_component;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.Frequency;
import com.project.core_service.models.shared.VersionedSchema;

import lombok.Data;

@Document
@Data
public class SystemComponent implements VersionedSchema {
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
    private boolean isOwnedByUs;

    private boolean isCICDUsed;

    @NonNull
    private CustomizationLevel customizationLevel;

    @NonNull
    private UpgradeStrategy upgradeStrategy;

    @NonNull
    private Frequency upgradeFrequency;

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
    private SecurityDetails securityDetails;

    private int version;
}
