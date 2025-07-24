package com.project.core_service.models.process_compliance;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
@Document
public class ProcessCompliant {
    @Id
    private String id;

    @NonNull
    private StandardGuideline standardGuideline;

    @NonNull
    private String description;
}
