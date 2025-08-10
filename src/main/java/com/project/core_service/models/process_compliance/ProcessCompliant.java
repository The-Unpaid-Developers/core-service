package com.project.core_service.models.process_compliance;

import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.VersionedSchema;

import lombok.Data;

@Data
@Document
@AllArgsConstructor
public class ProcessCompliant implements VersionedSchema {
    @Id
    private String id;

    @NonNull
    private StandardGuideline standardGuideline;

    @NonNull
    private Compliant compliant;

    @NonNull
    private String description;

    private int version;
}
