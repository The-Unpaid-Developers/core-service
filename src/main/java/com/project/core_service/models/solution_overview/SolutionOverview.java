package com.project.core_service.models.solution_overview;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.VersionedSchema;

import lombok.Data;

@Data
@Document
@AllArgsConstructor
@Builder
public class SolutionOverview implements VersionedSchema {
    @Id
    private String id;
    @NonNull
    private SolutionDetails solutionDetails;

    @NonNull
    private List<String> itBusinessPartners;

    @NonNull
    private String reviewedBy; // Potentially User if we are modelling user

    @NonNull
    private ReviewType reviewType;

    @NonNull
    private ApprovalStatus approvalStatus;

    @NonNull
    private ReviewStatus reviewStatus;

    private String conditions;

    @NonNull
    private BusinessUnit businessUnit;

    @NonNull
    private BusinessDriver businessDriver;

    @NonNull
    private String valueOutcome;

    @NonNull
    @Builder.Default
    private List<ApplicationUser> applicationUsers = new ArrayList<>();

    private List<Concern> concerns;

    private int version;
}
