package com.project.core_service.models.business_capabilities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Document
@Data
public class BusinessCapability {
    @Id
    private String id;

    @NonNull
    private String l1Capability;
    @NonNull
    private String l2Capability;
    @NonNull
    private String l3Capability;

    private String remarks;
}
