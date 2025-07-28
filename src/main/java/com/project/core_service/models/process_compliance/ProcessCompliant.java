package com.project.core_service.models.process_compliance;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.VersionedSchema;

import lombok.Data;

@Data
@Document
public class ProcessCompliant implements VersionedSchema {
    @Id
    private String id;

    @NonNull
    private StandardGuideline standardGuideline;

    @NonNull
    private String description;

    private int version;
}
