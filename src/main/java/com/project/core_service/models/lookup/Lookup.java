package com.project.core_service.models.lookup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lookup {
    private String id;

    private String lookupName;

    private List<Map<String, String>> data;

    private Date uploadedAt;

    private Integer recordCount;

    private String description;

    private Map<String, String> fieldsDescription;
}