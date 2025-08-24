package com.project.core_service.models.solution_overview;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SolutionDetails {
    @Nonnull
    private String solutionName;
    @Nonnull
    private String projectName;
    @Nonnull
    private String solutionReviewCode; // AWG Code
    @Nonnull
    private String solutionArchitectName;
    @Nonnull
    private String deliveryProjectManagerName;
    @Nonnull
    private String itBusinessPartner;
}
