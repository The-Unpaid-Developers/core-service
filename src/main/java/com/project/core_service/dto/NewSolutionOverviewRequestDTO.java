package com.project.core_service.dto;

import com.project.core_service.models.solution_overview.*;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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

    private List<Concern> concerns;

    public SolutionOverview toNewDraftEntity() {
        SolutionDetails copiedDetails = new SolutionDetails(
                solutionDetails.getSolutionName(),
                solutionDetails.getProjectName(),
                solutionDetails.getSolutionReviewCode(),
                solutionDetails.getSolutionArchitectName(),
                solutionDetails.getDeliveryProjectManagerName(),
                solutionDetails.getItBusinessPartner());
        return SolutionOverview.newDraftBuilder()
                .solutionDetails(copiedDetails)
                .businessUnit(businessUnit)
                .businessDriver(businessDriver)
                .valueOutcome(valueOutcome)
                .concerns(concerns == null ? List.of() : new ArrayList<>(concerns)) // copy list
                .build();
    }
}
