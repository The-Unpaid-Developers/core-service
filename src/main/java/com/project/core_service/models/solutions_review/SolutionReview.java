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

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "solutionReviews")
public class SolutionReview implements VersionedSchema {
    @Id
    private String id;

    @NonNull
    private DocumentState documentState;

    @NonNull
    private SolutionOverview solutionOverview;

    @NonNull
    @Builder.Default
    private List<BusinessCapability> businessCapabilities = new ArrayList<>();

    @NonNull
    @Builder.Default
    private List<SystemComponent> systemComponents = new ArrayList<>();

    @NonNull
    @Builder.Default
    private List<IntegrationFlow> integrationFlows = new ArrayList<>();

    @NonNull
    @Builder.Default
    private List<DataAsset> dataAssets = new ArrayList<>();

    @NonNull
    @Builder.Default
    private List<TechnologyComponent> technologyComponents = new ArrayList<>();

    @NonNull
    @Builder.Default
    private List<EnterpriseTool> enterpriseTools = new ArrayList<>();

    @NonNull
    @Builder.Default
    private List<ProcessCompliant> processCompliances = new ArrayList<>();

    private int version;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

    private String createdBy;

    private String lastModifiedBy;

    // Constructor for creating a new SolutionReview
    public SolutionReview(SolutionOverview solutionOverview) {
        this.documentState = DocumentState.DRAFT;
        this.solutionOverview = solutionOverview;
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
        this.businessCapabilities = businessCapabilities != null ? businessCapabilities : this.businessCapabilities;
        this.systemComponents = systemComponents != null ? systemComponents : this.systemComponents;
        this.integrationFlows = integrationFlows != null ? integrationFlows : this.integrationFlows;
        this.dataAssets = dataAssets != null ? dataAssets : this.dataAssets;
        this.technologyComponents = technologyComponents != null ? technologyComponents : this.technologyComponents;
        this.enterpriseTools = enterpriseTools != null ? enterpriseTools : this.enterpriseTools;
        this.processCompliances = processCompliances != null ? processCompliances : this.processCompliances;
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
        this.businessCapabilities.add(capability);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addSystemComponent(SystemComponent component) {
        this.systemComponents.add(component);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addIntegrationFlow(IntegrationFlow flow) {
        this.integrationFlows.add(flow);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addDataAsset(DataAsset asset) {
        this.dataAssets.add(asset);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addTechnologyComponent(TechnologyComponent component) {
        this.technologyComponents.add(component);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addEnterpriseTool(EnterpriseTool tool) {
        this.enterpriseTools.add(tool);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addProcessCompliance(ProcessCompliant compliant) {
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
                !this.businessCapabilities.isEmpty() &&
                !this.systemComponents.isEmpty();
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

    // Builder pattern factory methods
    public static SolutionReviewBuilder newDraftBuilder() {
        return SolutionReview.builder()
                .documentState(DocumentState.DRAFT)
                .version(1)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now());
        // Collections are automatically initialized via @Builder.Default
    }

    public static SolutionReviewBuilder builderFromSolutionOverview(SolutionOverview solutionOverview) {
        return newDraftBuilder()
                .solutionOverview(solutionOverview);
    }
}
