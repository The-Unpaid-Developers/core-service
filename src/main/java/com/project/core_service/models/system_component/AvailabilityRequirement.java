package com.project.core_service.models.system_component;

public enum AvailabilityRequirement {
    // > 99.999%
    HIGH,
    // 99% <= x <= 99.990%
    MEDIUM,
    // < 99%
    LOW
}
