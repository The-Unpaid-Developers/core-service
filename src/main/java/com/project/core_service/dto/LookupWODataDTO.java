package com.project.core_service.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Lookup without data and field descriptions fields.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LookupWODataDTO {
    private String id;
    private String lookupName;
    private Date uploadedAt;
    private Integer recordCount;
    private String description;
}
