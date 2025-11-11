package com.project.core_service.dto;

import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.solutions_review.DocumentState;

import jakarta.annotation.Nonnull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleanSolutionReviewDTO {
    private String id;
    @Nonnull
    private String systemCode;
    private DocumentState documentState;
    private SolutionOverview solutionOverview;
}
