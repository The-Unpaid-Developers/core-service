package com.project.core_service.dto;

import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.solutions_review.SolutionReview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for active Solution Reviews.
 * Contains only the essential fields: systemCode, solutionOverview, and businessCapabilities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessCapabilityDTO {
    private String systemCode;
    private SolutionOverview solutionOverview;
    private List<BusinessCapability> businessCapabilities;

    /**
     * Factory method to create a BusinessCapabilityDTO from a SolutionReview entity.
     *
     * @param solutionReview the solution review entity
     * @return a new BusinessCapabilityDTO instance
     */
    public static BusinessCapabilityDTO fromSolutionReview(SolutionReview solutionReview) {
        return new BusinessCapabilityDTO(
                solutionReview.getSystemCode(),
                solutionReview.getSolutionOverview(),
                solutionReview.getBusinessCapabilities()
        );
    }
}