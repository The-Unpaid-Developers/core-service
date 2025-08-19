package com.project.core_service.models.business_capabilities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;

@Document
@AllArgsConstructor
@Data
public class BusinessCapability {
    @Id
    private String id;

    @NonNull
    private L1Capability l1Capability;
    @NonNull
    private L2Capability l2Capability;
    @NonNull
    private L3Capability l3Capability;

    private String remarks;
}
