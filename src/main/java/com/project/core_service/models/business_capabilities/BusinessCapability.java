package com.project.core_service.models.business_capabilities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

@Document
public class BusinessCapability {
    @Id
    private String id;

    @NonNull
    private String l1Capability;
    @NonNull
    private String l2Capability;
    @NonNull
    private String l3Capability;
    @NonNull
    private String remarks;
}
