package com.project.core_service.models.process_compliance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
@Document
@AllArgsConstructor
@Builder
public class ProcessCompliant {
    @Id
    private String id;

    @NonNull
    private StandardGuideline standardGuideline;

    @NonNull
    private Compliant compliant;

    @NonNull
    private String description;

}
