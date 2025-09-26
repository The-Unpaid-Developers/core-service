package com.project.core_service.models.solution_overview;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
@Document
@AllArgsConstructor
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

    @NonNull
    private LocalDateTime followUpDate;
}
