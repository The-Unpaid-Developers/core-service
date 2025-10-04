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

    private String remarks;

    private List<Capability> capabilities;
}
