package com.project.core_service.models.integration_flow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.Frequency;

import lombok.Data;

@Document
@Data
@AllArgsConstructor
@Builder
public class IntegrationFlow {
    @Id
    private String id;

    @NonNull
    private String counterpartSystemCode;

    @NonNull
    private CounterpartSystemRole counterpartSystemRole;

    @NonNull
    private IntegrationMethod integrationMethod;

    @NonNull
    private Frequency frequency;

    @NonNull
    private String purpose;

}
