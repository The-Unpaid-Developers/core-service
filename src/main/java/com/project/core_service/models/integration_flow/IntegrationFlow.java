package com.project.core_service.models.integration_flow;

import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.Frequency;

import lombok.Data;

@Document
@Data
@AllArgsConstructor
public class IntegrationFlow {
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

}
