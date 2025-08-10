package com.project.core_service.models.solutions_review;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.data_asset.DataAsset;
import com.project.core_service.models.enterprise_tools.EnterpriseTool;
import com.project.core_service.models.integration_flow.IntegrationFlow;
import com.project.core_service.models.process_compliance.ProcessCompliant;
import com.project.core_service.models.shared.VersionedSchema;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.system_component.SystemComponent;
import com.project.core_service.models.technology_component.TechnologyComponent;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "solutionReviews")
public class SolutionReview implements VersionedSchema {
    @Id
    private String id;

    @NonNull
    private DocumentState documentState;

    @NonNull
    private SolutionOverview solutionOverview;

    @NonNull
    private List<BusinessCapability> businessCapabilities;

    @NonNull
    private List<SystemComponent> systemComponents;

    @NonNull
    private List<IntegrationFlow> integrationFlows;

    @NonNull
    private List<DataAsset> dataAssets;

    @NonNull
    private List<TechnologyComponent> technologyComponents;

    @NonNull
    private List<EnterpriseTool> enterpriseTools;

    @NonNull
    private List<ProcessCompliant> processCompliances;

    private int version;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

    private String createdBy;

    private String lastModifiedBy;

    // Constructor for creating a new SolutionReview
    public SolutionReview(SolutionOverview solutionOverview) {
        this.documentState = DocumentState.DRAFT;
        this.solutionOverview = solutionOverview;
        this.businessCapabilities = new ArrayList<>();
        this.systemComponents = new ArrayList<>();
        this.integrationFlows = new ArrayList<>();
        this.dataAssets = new ArrayList<>();
        this.technologyComponents = new ArrayList<>();
        this.enterpriseTools = new ArrayList<>();
        this.processCompliances = new ArrayList<>();
        this.version = 1; // default till we update schema
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    // Full constructor for creating a complete SolutionReview
    public SolutionReview(DocumentState documentState,
            SolutionOverview solutionOverview,
            List<BusinessCapability> businessCapabilities,
            List<SystemComponent> systemComponents,
            List<IntegrationFlow> integrationFlows,
            List<DataAsset> dataAssets,
            List<TechnologyComponent> technologyComponents,
            List<EnterpriseTool> enterpriseTools,
            List<ProcessCompliant> processCompliances,
            String createdBy) {
        this.documentState = documentState;
        this.solutionOverview = solutionOverview;
        this.businessCapabilities = businessCapabilities != null ? businessCapabilities : new ArrayList<>();
        this.systemComponents = systemComponents != null ? systemComponents : new ArrayList<>();
        this.integrationFlows = integrationFlows != null ? integrationFlows : new ArrayList<>();
        this.dataAssets = dataAssets != null ? dataAssets : new ArrayList<>();
        this.technologyComponents = technologyComponents != null ? technologyComponents : new ArrayList<>();
        this.enterpriseTools = enterpriseTools != null ? enterpriseTools : new ArrayList<>();
        this.processCompliances = processCompliances != null ? processCompliances : new ArrayList<>();
        this.version = 1; // default till we update schema
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.lastModifiedBy = createdBy;
    }

    // Copy constructor for creating a new version
    public SolutionReview(SolutionReview original, String modifiedBy) {
        this.documentState = original.documentState;
        this.solutionOverview = original.solutionOverview;
        this.businessCapabilities = new ArrayList<>(original.businessCapabilities);
        this.systemComponents = new ArrayList<>(original.systemComponents);
        this.integrationFlows = new ArrayList<>(original.integrationFlows);
        this.dataAssets = new ArrayList<>(original.dataAssets);
        this.technologyComponents = new ArrayList<>(original.technologyComponents);
        this.enterpriseTools = new ArrayList<>(original.enterpriseTools);
        this.processCompliances = new ArrayList<>(original.processCompliances);
        this.version = original.version + 1;
        this.createdAt = original.createdAt;
        this.lastModifiedAt = LocalDateTime.now();
        this.createdBy = original.createdBy;
        this.lastModifiedBy = modifiedBy;
    }

    // Utility methods for document state management
    public void submit() {
        if (this.documentState == DocumentState.DRAFT) {
            this.documentState = DocumentState.SUBMITTED;
            this.lastModifiedAt = LocalDateTime.now();
        }
    }

    public void approve() {
        if (this.documentState == DocumentState.SUBMITTED) {
            this.documentState = DocumentState.CURRENT;
            this.lastModifiedAt = LocalDateTime.now();
        }
    }

    public void markAsOutdated() {
        if (this.documentState == DocumentState.CURRENT) {
            this.documentState = DocumentState.OUTDATED;
            this.lastModifiedAt = LocalDateTime.now();
        }
    }

    // TODO: @yuezhen - implement more complicated lifecycle methods

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

    // Utility methods for managing lists
    public void addBusinessCapability(BusinessCapability capability) {
        if (this.businessCapabilities == null) {
            this.businessCapabilities = new ArrayList<>();
        }
        this.businessCapabilities.add(capability);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addSystemComponent(SystemComponent component) {
        if (this.systemComponents == null) {
            this.systemComponents = new ArrayList<>();
        }
        this.systemComponents.add(component);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addIntegrationFlow(IntegrationFlow flow) {
        if (this.integrationFlows == null) {
            this.integrationFlows = new ArrayList<>();
        }
        this.integrationFlows.add(flow);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addDataAsset(DataAsset asset) {
        if (this.dataAssets == null) {
            this.dataAssets = new ArrayList<>();
        }
        this.dataAssets.add(asset);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addTechnologyComponent(TechnologyComponent component) {
        if (this.technologyComponents == null) {
            this.technologyComponents = new ArrayList<>();
        }
        this.technologyComponents.add(component);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addEnterpriseTool(EnterpriseTool tool) {
        if (this.enterpriseTools == null) {
            this.enterpriseTools = new ArrayList<>();
        }
        this.enterpriseTools.add(tool);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addProcessCompliance(ProcessCompliant compliant) {
        if (this.processCompliances == null) {
            this.processCompliances = new ArrayList<>();
        }
        this.processCompliances.add(compliant);
        this.lastModifiedAt = LocalDateTime.now();
    }

    // Update modification tracking
    public void updateModification(String modifiedBy) {
        this.lastModifiedAt = LocalDateTime.now();
        this.lastModifiedBy = modifiedBy;
    }

    // Check if the solution review is complete (all required lists have items)
    public boolean isComplete() {
        return this.solutionOverview != null &&
                this.businessCapabilities != null && !this.businessCapabilities.isEmpty() &&
                this.systemComponents != null && !this.systemComponents.isEmpty();
    }

    // VersionedSchema implementation
    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
        this.lastModifiedAt = LocalDateTime.now();
    }
}
