package com.project.core_service.models.solution_overview;

import java.util.List;

import jakarta.annotation.Nonnull;
import lombok.Data;

@Data
public class SolutionDetails {
    @Nonnull
    private String solutionName;
    @Nonnull
    private String systemCode;
    @Nonnull
    private String projectName;
    @Nonnull
    private String solutionReviewCode; // AWG Code
    @Nonnull
    private String solutionArchitectName;
    @Nonnull
    private String deliveryProjectManagerName;
    @Nonnull
    private List<String> itBusinessPartners;
}
