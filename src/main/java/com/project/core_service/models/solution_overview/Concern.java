package com.project.core_service.models.solution_overview;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Document
@Data
public class Concern {
    @Id
    private String id;
    @NonNull
    private ConcernType type;

    @NonNull
    private String description;

    @NonNull
    private String impact;

    @NonNull
    private String disposition;

    @NonNull
    private ConcernStatus status;
}
