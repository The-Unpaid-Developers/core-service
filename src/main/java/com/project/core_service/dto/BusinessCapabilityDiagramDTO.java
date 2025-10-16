package com.project.core_service.dto;

import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.solutions_review.SolutionReview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for active Solution Reviews used in business capability diagrams.
 * Contains only the essential fields: systemCode, solutionOverview, and businessCapabilities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessCapabilityDiagramDTO {
    private String systemCode;
    private SolutionOverview solutionOverview;
    private List<BusinessCapability> businessCapabilities;

    /**
     * Factory method to create a BusinessCapabilityDiagramDTO from a SolutionReview entity.
     *
     * @param solutionReview the solution review entity
     * @return a new BusinessCapabilityDiagramDTO instance
     * @throws IllegalArgumentException if solutionReview is null
     */
    public static BusinessCapabilityDiagramDTO fromSolutionReview(SolutionReview solutionReview) {
        if (solutionReview == null) {
            throw new IllegalArgumentException("SolutionReview cannot be null");
        }

        return new BusinessCapabilityDiagramDTO(
                solutionReview.getSystemCode(),
                solutionReview.getSolutionOverview(),
                solutionReview.getBusinessCapabilities()
        );
    }
}