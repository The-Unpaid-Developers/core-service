package com.project.core_service.models.solution_overview;

import java.util.List;

import jakarta.annotation.Nonnull;

public record SolutionDetails(
        @Nonnull String solutionName,
        @Nonnull String systemCode,
        @Nonnull String projectName,
        @Nonnull String solutionReviewCode, // AWG Code
        @Nonnull String solutionArchitectName,
        @Nonnull String deliveryProjectManagerName,
        @Nonnull List<String> itBusinessPartners) {
}
