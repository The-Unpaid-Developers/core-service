package com.project.core_service.models.enterprise_tools;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.VersionedSchema;

import lombok.Data;

@Data
@Document
public class Tool implements VersionedSchema {
    @Id
    private String id;

    @NonNull
    private String name;

    @NonNull
    private ToolType type;

    private int version;
}
