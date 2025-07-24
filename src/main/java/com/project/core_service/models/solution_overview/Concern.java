package com.project.core_service.models.solution_overview;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.annotation.Nonnull;
import lombok.Data;

@Document
@Data
public class Concern {
    @Id
    private String id;
    @Nonnull
    private ConcernType type;

    @Nonnull
    private String description;

    @Nonnull
    private String impact;

    @Nonnull
    private String disposition;

    @Nonnull
    private String status;
}
