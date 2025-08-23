package com.project.core_service.models.technology_component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Document
@Data
@AllArgsConstructor
@Builder
// TODO: Does this overlap with system_component.LanguageFramework?
public class TechnologyComponent {
    @Id
    private String id;

    private String componentName;

    @NonNull
    private String productName;

    @NonNull
    private String productVersion;

    @NonNull
    private Usage usage;

}
