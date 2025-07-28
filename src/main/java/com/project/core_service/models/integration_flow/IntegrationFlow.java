package com.project.core_service.models.integration_flow;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.Frequency;
import com.project.core_service.models.shared.VersionedSchema;

import lombok.Data;

@Document
@Data
public class IntegrationFlow implements VersionedSchema {
    @Id
    private String id;

    @NonNull
    private String bsoCodeOfExternalSystem;

    @NonNull
    private ExternalSystemRole externalSystemRole;

    @NonNull
    private IntegrationMethod integrationMethod;

    @NonNull
    private Frequency frequency;

    @NonNull
    private String purpose;

    private int version;
}
