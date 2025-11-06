package com.project.core_service.models.lookup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mongodb.lang.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lookup {
    @NonNull
    private String id;

    @NonNull
    private String lookupName;

    @NonNull
    private List<Map<String, String>> data;

    @NonNull
    private Date uploadedAt;

    @NonNull
    private Integer recordCount;

    @NonNull
    private String description;

    @NonNull
    private Map<String, String> fieldDescriptions;
}