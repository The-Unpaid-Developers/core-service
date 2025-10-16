package com.project.core_service.models.business_capabilities;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Document
@AllArgsConstructor
@Data
@Builder
public class BusinessCapability {
    @Id
    private String id;

    private L1Capability l1Capability;

    private L2Capability l2Capability;

    private L3Capability l3Capability;

    private String remarks;
    
}
