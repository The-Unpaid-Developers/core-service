package com.project.core_service.models.enterprise_tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Document
@Data
@AllArgsConstructor
@Builder
public class EnterpriseTool {
    @Id
    private String id;

    @NonNull
    @DBRef
    private Tool tool;

    @NonNull
    private OnboardingStatus onboarded;

    // Configurations, pipelines, APIs
    // Explanation if tool not onboarded
    private String integrationDetails;

    // Blockers, non-compliance, risk notes
    @NonNull
    private String issues;

}
