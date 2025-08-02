package com.project.core_service.models.system_component;

public enum AvailabilityRequirement {
    // > 99.999%
    VERY_HIGH,
    // 99% <= x <= 99.990%
    HIGH,
    // < 99%
    MEDIUM
}
