package com.project.core_service.models.business_capabilities;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;

@Document
@AllArgsConstructor
@Data
@Builder
public class BusinessCapability {
    @Id
    private String id;

    private String l1Capability;

    private String l2Capability;

    private String l3Capability;

    private String remarks;
    
}
