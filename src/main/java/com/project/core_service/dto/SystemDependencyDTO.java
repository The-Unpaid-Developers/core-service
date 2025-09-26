package com.project.core_service.dto;

import com.project.core_service.models.integration_flow.IntegrationFlow;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.solutions_review.SolutionReview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for active Solution Reviews.
 * Contains only the essential fields: systemCode, solutionOverview, and integrationFlows.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemDependencyDTO {
    private String systemCode;
    private SolutionOverview solutionOverview;
    private List<IntegrationFlow> integrationFlows;

    /**
     * Factory method to create an SystemDependencyDTO from a SolutionReview entity.
     *
     * @param solutionReview the solution review entity
     * @return a new SystemDependencyDTO instance
     */
    public static SystemDependencyDTO fromSolutionReview(SolutionReview solutionReview) {
        return new SystemDependencyDTO(
                solutionReview.getSystemCode(),
                solutionReview.getSolutionOverview(),
                solutionReview.getIntegrationFlows()
        );
    }
}