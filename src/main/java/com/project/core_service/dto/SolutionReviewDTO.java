package com.project.core_service.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.data_asset.DataAsset;
import com.project.core_service.models.enterprise_tools.EnterpriseTool;
import com.project.core_service.models.integration_flow.IntegrationFlow;
import com.project.core_service.models.process_compliance.ProcessCompliant;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.models.system_component.SystemComponent;
import com.project.core_service.models.technology_component.TechnologyComponent;

import jakarta.annotation.Nonnull;
import lombok.*;

/**
 * Data Transfer Object for SolutionReview
 * Used for API responses and requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolutionReviewDTO {
    private String id;
    @Nonnull
    private String systemCode;
    private DocumentState documentState;
    private SolutionOverview solutionOverview;

    @Builder.Default
    private List<BusinessCapability> businessCapabilities = new ArrayList<>();

    @Builder.Default
    private List<SystemComponent> systemComponents = new ArrayList<>();

    @Builder.Default
    private List<IntegrationFlow> integrationFlows = new ArrayList<>();

    @Builder.Default
    private List<DataAsset> dataAssets = new ArrayList<>();

    @Builder.Default
    private List<TechnologyComponent> technologyComponents = new ArrayList<>();

    @Builder.Default
    private List<EnterpriseTool> enterpriseTools = new ArrayList<>();

    @Builder.Default
    private List<ProcessCompliant> processCompliances = new ArrayList<>();

    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private String createdBy;
    private String lastModifiedBy;

    // Constructor to convert from SolutionReview entity with null safety
    public SolutionReviewDTO(SolutionReview solutionReview) {
        if (solutionReview == null) {
            throw new IllegalArgumentException("SolutionReview cannot be null");
        }

        this.id = solutionReview.getId();
        this.systemCode = solutionReview.getSystemCode();
        this.documentState = solutionReview.getDocumentState();
        this.solutionOverview = solutionReview.getSolutionOverview();

        // Defensive copying for lists - use defaults if source is null
        this.businessCapabilities = solutionReview.getBusinessCapabilities() != null
                ? new ArrayList<>(solutionReview.getBusinessCapabilities())
                : this.businessCapabilities; // Keep default empty list
        this.systemComponents = solutionReview.getSystemComponents() != null
                ? new ArrayList<>(solutionReview.getSystemComponents())
                : this.systemComponents;
        this.integrationFlows = solutionReview.getIntegrationFlows() != null
                ? new ArrayList<>(solutionReview.getIntegrationFlows())
                : this.integrationFlows;
        this.dataAssets = solutionReview.getDataAssets() != null
                ? new ArrayList<>(solutionReview.getDataAssets())
                : this.dataAssets;
        this.technologyComponents = solutionReview.getTechnologyComponents() != null
                ? new ArrayList<>(solutionReview.getTechnologyComponents())
                : this.technologyComponents;
        this.enterpriseTools = solutionReview.getEnterpriseTools() != null
                ? new ArrayList<>(solutionReview.getEnterpriseTools())
                : this.enterpriseTools;
        this.processCompliances = solutionReview.getProcessCompliances() != null
                ? new ArrayList<>(solutionReview.getProcessCompliances())
                : this.processCompliances;

        this.version = solutionReview.getVersion();
        this.createdAt = solutionReview.getCreatedAt();
        this.lastModifiedAt = solutionReview.getLastModifiedAt();
        this.createdBy = solutionReview.getCreatedBy();
        this.lastModifiedBy = solutionReview.getLastModifiedBy();
    }

    // Convert DTO back to entity with defensive copying
    public SolutionReview toEntity() {
        SolutionReview entity = new SolutionReview();
        entity.setId(this.id);
        entity.setSystemCode(this.systemCode);
        entity.setDocumentState(this.documentState);
        entity.setSolutionOverview(this.solutionOverview);

        // Defensive copying for lists - fields are never null due to @Builder.Default
        entity.setBusinessCapabilities(new ArrayList<>(this.businessCapabilities));
        entity.setSystemComponents(new ArrayList<>(this.systemComponents));
        entity.setIntegrationFlows(new ArrayList<>(this.integrationFlows));
        entity.setDataAssets(new ArrayList<>(this.dataAssets));
        entity.setTechnologyComponents(new ArrayList<>(this.technologyComponents));
        entity.setEnterpriseTools(new ArrayList<>(this.enterpriseTools));
        entity.setProcessCompliances(new ArrayList<>(this.processCompliances));

        entity.setVersion(this.version);
        entity.setCreatedAt(this.createdAt);
        entity.setLastModifiedAt(this.lastModifiedAt);
        entity.setCreatedBy(this.createdBy);
        entity.setLastModifiedBy(this.lastModifiedBy);
        return entity;
    }

    // Utility methods
    public boolean isDraft() {
        return this.documentState == DocumentState.DRAFT;
    }

    public boolean isSubmitted() {
        return this.documentState == DocumentState.SUBMITTED;
    }

    public boolean isCurrent() {
        return this.documentState == DocumentState.CURRENT;
    }

    public boolean isOutdated() {
        return this.documentState == DocumentState.OUTDATED;
    }

    // Factory method to create DTO from entity with null safety
    public static SolutionReviewDTO fromEntity(SolutionReview solutionReview) {
        if (solutionReview == null) {
            return null;
        }
        return new SolutionReviewDTO(solutionReview);
    }

    // Builder pattern factory methods for common use cases
    public static SolutionReviewDTOBuilder newDraftBuilder(String systemCode) {
        return SolutionReviewDTO.builder()
                .systemCode(systemCode)
                .documentState(DocumentState.DRAFT)
                .version(1)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now());
    }

    public static SolutionReviewDTOBuilder builderFromSolutionOverview(String systemCode, SolutionOverview solutionOverview) {
        Objects.requireNonNull(systemCode, "SystemCode cannot be null");
        Objects.requireNonNull(solutionOverview, "SolutionOverview cannot be null");

        return newDraftBuilder(systemCode)
                .solutionOverview(solutionOverview);
    }

    public boolean hasValidSolutionOverview() {
        return this.solutionOverview != null;
    }

    public boolean hasBusinessCapabilities() {
        return !this.businessCapabilities.isEmpty();
    }

    public boolean hasSystemComponents() {
        return !this.systemComponents.isEmpty();
    }

}
