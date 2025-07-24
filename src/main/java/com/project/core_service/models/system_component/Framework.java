package com.project.core_service.models.system_component;

import lombok.Data;
import lombok.NonNull;

@Data
public class Framework {
    @NonNull
    private String name;

    @NonNull
    private String version;
}
