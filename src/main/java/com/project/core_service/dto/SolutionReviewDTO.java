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

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
    private DocumentState documentState;
    private SolutionOverview solutionOverview;
    private List<BusinessCapability> businessCapabilities;
    private List<SystemComponent> systemComponents;
    private List<IntegrationFlow> integrationFlows;
    private List<DataAsset> dataAssets;
    private List<TechnologyComponent> technologyComponents;
    private List<EnterpriseTool> enterpriseTools;
    private List<ProcessCompliant> processCompliances;
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
        this.documentState = solutionReview.getDocumentState();
        this.solutionOverview = solutionReview.getSolutionOverview();

        // Defensive copying for lists to prevent external modification
        this.businessCapabilities = solutionReview.getBusinessCapabilities() != null
                ? new ArrayList<>(solutionReview.getBusinessCapabilities())
                : new ArrayList<>();
        this.systemComponents = solutionReview.getSystemComponents() != null
                ? new ArrayList<>(solutionReview.getSystemComponents())
                : new ArrayList<>();
        this.integrationFlows = solutionReview.getIntegrationFlows() != null
                ? new ArrayList<>(solutionReview.getIntegrationFlows())
                : new ArrayList<>();
        this.dataAssets = solutionReview.getDataAssets() != null
                ? new ArrayList<>(solutionReview.getDataAssets())
                : new ArrayList<>();
        this.technologyComponents = solutionReview.getTechnologyComponents() != null
                ? new ArrayList<>(solutionReview.getTechnologyComponents())
                : new ArrayList<>();
        this.enterpriseTools = solutionReview.getEnterpriseTools() != null
                ? new ArrayList<>(solutionReview.getEnterpriseTools())
                : new ArrayList<>();
        this.processCompliances = solutionReview.getProcessCompliances() != null
                ? new ArrayList<>(solutionReview.getProcessCompliances())
                : new ArrayList<>();

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
        entity.setDocumentState(this.documentState);
        entity.setSolutionOverview(this.solutionOverview);

        // Defensive copying for lists to prevent external modification
        entity.setBusinessCapabilities(this.businessCapabilities != null
                ? new ArrayList<>(this.businessCapabilities)
                : new ArrayList<>());
        entity.setSystemComponents(this.systemComponents != null
                ? new ArrayList<>(this.systemComponents)
                : new ArrayList<>());
        entity.setIntegrationFlows(this.integrationFlows != null
                ? new ArrayList<>(this.integrationFlows)
                : new ArrayList<>());
        entity.setDataAssets(this.dataAssets != null
                ? new ArrayList<>(this.dataAssets)
                : new ArrayList<>());
        entity.setTechnologyComponents(this.technologyComponents != null
                ? new ArrayList<>(this.technologyComponents)
                : new ArrayList<>());
        entity.setEnterpriseTools(this.enterpriseTools != null
                ? new ArrayList<>(this.enterpriseTools)
                : new ArrayList<>());
        entity.setProcessCompliances(this.processCompliances != null
                ? new ArrayList<>(this.processCompliances)
                : new ArrayList<>());

        entity.setVersion(this.version);
        entity.setCreatedAt(this.createdAt);
        entity.setLastModifiedAt(this.lastModifiedAt);
        entity.setCreatedBy(this.createdBy);
        entity.setLastModifiedBy(this.lastModifiedBy);
        return entity;
    }

    // Utility methods
    public boolean isComplete() {
        return this.solutionOverview != null &&
                this.businessCapabilities != null && !this.businessCapabilities.isEmpty() &&
                this.systemComponents != null && !this.systemComponents.isEmpty();
    }

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

    // Builder pattern example factory methods for common use cases
    public static SolutionReviewDTOBuilder newDraftBuilder() {
        return SolutionReviewDTO.builder()
                .documentState(DocumentState.DRAFT)
                .version(1)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .businessCapabilities(new ArrayList<>())
                .systemComponents(new ArrayList<>())
                .integrationFlows(new ArrayList<>())
                .dataAssets(new ArrayList<>())
                .technologyComponents(new ArrayList<>())
                .enterpriseTools(new ArrayList<>())
                .processCompliances(new ArrayList<>());
    }

    public static SolutionReviewDTOBuilder builderFromSolutionOverview(SolutionOverview solutionOverview) {
        Objects.requireNonNull(solutionOverview, "SolutionOverview cannot be null");

        return newDraftBuilder()
                .solutionOverview(solutionOverview);
    }

    // Validation methods with null safety
    public boolean hasValidSolutionOverview() {
        return this.solutionOverview != null;
    }

    public boolean hasBusinessCapabilities() {
        return this.businessCapabilities != null && !this.businessCapabilities.isEmpty();
    }

    public boolean hasSystemComponents() {
        return this.systemComponents != null && !this.systemComponents.isEmpty();
    }

}
