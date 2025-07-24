package com.project.core_service.models.solution_overview;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import jakarta.annotation.Nonnull;
import lombok.Data;

@Data
@Document
public class SolutionOverview {
    @Id
    private String id;
    @NonNull
    private String solutionName;
    @NonNull
    private String systemCode;
    @NonNull
    private String projectName;
    @NonNull
    private String solutionReviewCode; // AWG Code
    @NonNull
    private String solutionArchitectName;
    @NonNull
    private String deliveryProjectManagerName;

    @NonNull
    private List<String> itBusinessPartners;

    @NonNull
    private String reviewedBy; // Potentially User if we are modelling user

    @NonNull
    private ReviewType reviewType;

    @NonNull
    private ReviewStatus reviewStatus;

    private String conditions;

    @NonNull
    private BusinessUnit businessUnit;

    @Nonnull
    private BusinessDriver businessDriver;

    @NonNull
    private String valueOutcome;

    @NonNull
    private List<TargetUser> targetUsers;

    private List<Concern> concerns;
}
