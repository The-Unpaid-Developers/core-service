package com.project.core_service.dto;

import java.time.LocalDateTime;
import java.util.List;

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

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for SolutionReview
 * Used for API responses and requests
 */
@Data
@NoArgsConstructor
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
    private List<ProcessCompliant> processCompliants;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private String createdBy;
    private String lastModifiedBy;

    // Constructor to convert from SolutionReview entity
    public SolutionReviewDTO(SolutionReview solutionReview) {
        this.id = solutionReview.getId();
        this.documentState = solutionReview.getDocumentState();
        this.solutionOverview = solutionReview.getSolutionOverview();
        this.businessCapabilities = solutionReview.getBusinessCapabilities();
        this.systemComponents = solutionReview.getSystemComponents();
        this.integrationFlows = solutionReview.getIntegrationFlows();
        this.dataAssets = solutionReview.getDataAssets();
        this.technologyComponents = solutionReview.getTechnologyComponents();
        this.enterpriseTools = solutionReview.getEnterpriseTools();
        this.processCompliants = solutionReview.getProcessCompliants();
        this.version = solutionReview.getVersion();
        this.createdAt = solutionReview.getCreatedAt();
        this.lastModifiedAt = solutionReview.getLastModifiedAt();
        this.createdBy = solutionReview.getCreatedBy();
        this.lastModifiedBy = solutionReview.getLastModifiedBy();
    }

    // Convert DTO back to entity
    public SolutionReview toEntity() {
        SolutionReview entity = new SolutionReview();
        entity.setId(this.id);
        entity.setDocumentState(this.documentState);
        entity.setSolutionOverview(this.solutionOverview);
        entity.setBusinessCapabilities(this.businessCapabilities);
        entity.setSystemComponents(this.systemComponents);
        entity.setIntegrationFlows(this.integrationFlows);
        entity.setDataAssets(this.dataAssets);
        entity.setTechnologyComponents(this.technologyComponents);
        entity.setEnterpriseTools(this.enterpriseTools);
        entity.setProcessCompliants(this.processCompliants);
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
}
