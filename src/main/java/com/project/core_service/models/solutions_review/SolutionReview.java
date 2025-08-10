package com.project.core_service.models.solutions_review;

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

@Data
@Document
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
    private List<ProcessCompliant> processCompliants;

    private int version;
}
