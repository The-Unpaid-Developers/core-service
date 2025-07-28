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

    // true if it's a producer, else it's a consumer
    private boolean isProducer;

    @NonNull
    private IntegrationMethod integrationMethod;

    @NonNull
    private Frequency frequency;

    @NonNull
    private String purpose;

    private int version;
}
