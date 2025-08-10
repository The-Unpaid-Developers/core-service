package com.project.core_service.models.enterprise_tools;

import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.VersionedSchema;

import lombok.Data;

@Document
@Data
@AllArgsConstructor
public class EnterpriseTool implements VersionedSchema {
    @Id
    private String id;

    @NonNull
    private Tool tool;

    @NonNull
    private OnboardingStatus onboarded;

    // Configurations, pipelines, APIs
    // Explanation if tool not onboarded
    private String integrationStatus;

    // Blockers, non-compliance, risk notes
    @NonNull
    private String issues;

    @NonNull
    private String solutionOverviewId;

    private int version;
}
