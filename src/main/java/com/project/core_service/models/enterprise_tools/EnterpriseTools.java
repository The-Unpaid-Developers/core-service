package com.project.core_service.models.enterprise_tools;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Document
@Data
public class EnterpriseTools {
    @Id
    private String id;

    // TODO: Impl validation for tool type
    // ToolName will correspond to a specific tool type
    @NonNull
    private ToolType toolType;

    @NonNull
    private ToolName toolName;

    @NonNull
    private OnboardingStatus onboarded;

    // Configurations, pipelines, APIs
    // Explanation if tool not onboarded
    @NonNull
    private String integrationStatus;

    // Blockers, non-compliance, risk notes
    @NonNull
    private String issues;
}
