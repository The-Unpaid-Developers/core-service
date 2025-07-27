package com.project.core_service.models.integration_flow;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.annotation.Nonnull;
import lombok.Data;

@Document
@Data
public class IntegrationFlow {
    @Id
    private String id;

    @Nonnull
    private String bsoCodeOfExternalSystem;

    // true if it's a producer, else it's a consumer
    private boolean isProducer;

    @Nonnull
    private IntegrationMethod integrationMethod;

    @Nonnull
    private Frequency frequency;

    @Nonnull
    private String purpose;
}
