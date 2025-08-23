package com.project.core_service.dto;

import com.project.core_service.models.solution_overview.*;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewSolutionOverviewRequestDTO {
    @Nonnull
    private SolutionDetails solutionDetails;

    @Nonnull
    private BusinessUnit businessUnit;

    @Nonnull
    private BusinessDriver businessDriver;

    @Nonnull
    private String valueOutcome;

    public SolutionOverview toNewDraftEntity() {
        SolutionDetails copiedDetails = new SolutionDetails(
                solutionDetails.getSolutionName(),
                solutionDetails.getProjectName(),
                solutionDetails.getSolutionReviewCode(),
                solutionDetails.getSolutionArchitectName(),
                solutionDetails.getDeliveryProjectManagerName(),
                solutionDetails.getItBusinessPartner() // copy list
        );
        return SolutionOverview.newDraftBuilder()
                .solutionDetails(copiedDetails)
                .businessUnit(businessUnit)
                .businessDriver(businessDriver)
                .valueOutcome(valueOutcome)
                .build();
    }
}
