package com.project.core_service.dto;

import com.project.core_service.models.lookup.Lookup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LookupDTO {
    private boolean success;
    private String message;
    private String lookupName;
    private Integer recordsProcessed;
    private Integer totalLookups;
    private List<Lookup> lookups;
}