package com.project.core_service.models.solution_overview;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
@Document
@AllArgsConstructor
@Builder
public class SolutionOverview {
    @Id
    private String id;
    @NonNull
    private SolutionDetails solutionDetails;

    private String reviewedBy;

    @NonNull
    private ReviewType reviewType;

    @NonNull
    private ApprovalStatus approvalStatus;

    @NonNull
    private ReviewStatus reviewStatus;

    private String conditions;

    @NonNull
    private BusinessUnit businessUnit;

    @NonNull
    private BusinessDriver businessDriver;

    @NonNull
    private String valueOutcome;

    @NonNull
    @Builder.Default
    private List<ApplicationUser> applicationUsers = new ArrayList<>();

    private List<Concern> concerns;

    public static SolutionOverviewBuilder newDraftBuilder() {
        return new SolutionOverviewBuilder()
                .reviewType(ReviewType.NEW_BUILD)
                .approvalStatus(ApprovalStatus.PENDING)
                .reviewStatus(ReviewStatus.DRAFT);
    }
    public static SolutionOverviewBuilder fromExisting(SolutionOverview existing) {
        return new SolutionOverviewBuilder()
                .id(existing.getId())
                .solutionDetails(existing.getSolutionDetails())
                .reviewedBy(existing.getReviewedBy())
                .reviewType(existing.getReviewType())
                .approvalStatus(existing.getApprovalStatus())
                .reviewStatus(existing.getReviewStatus())
                .conditions(existing.getConditions())
                .businessUnit(existing.getBusinessUnit())
                .businessDriver(existing.getBusinessDriver())
                .valueOutcome(existing.getValueOutcome())
                .applicationUsers(existing.getApplicationUsers())
                .concerns(existing.getConcerns());
    }

    public static SolutionOverviewBuilder newEnhancementBuilder(SolutionOverview existing) {
        return fromExisting(existing)
                .reviewType(ReviewType.ENHANCEMENT)
                .approvalStatus(ApprovalStatus.PENDING)
                .reviewStatus(ReviewStatus.DRAFT);
    }

    public static SolutionOverviewBuilder newApprovedBuilder() {
        return new SolutionOverviewBuilder()
                .reviewType(ReviewType.NEW_BUILD)
                .approvalStatus(ApprovalStatus.APPROVED)
                .reviewStatus(ReviewStatus.COMPLETED);
    }
}

