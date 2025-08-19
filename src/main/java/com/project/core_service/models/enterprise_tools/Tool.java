package com.project.core_service.models.enterprise_tools;

import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
@Document
@AllArgsConstructor
public class Tool {
    @Id
    private String id;

    @NonNull
    private String name;

    @NonNull
    private ToolType type;
}
