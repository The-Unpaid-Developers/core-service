package com.project.core_service.models.business_capabilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.annotation.Nonnull;

@Data
@AllArgsConstructor
public class Capability {
    @Nonnull
    private String name;
    private CapabilityType capabilityType;
}
