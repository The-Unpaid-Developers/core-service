package com.project.core_service.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.mongodb.lang.NonNull;

/**
 * DTO for uploading context related to a lookup.
 * 
 * <p>
 * This DTO is used to accept client requests for uploading context related to a lookup,
 * preventing direct manipulation of the Query entity properties.
 * </p>
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LookupContextDTO {
    /*
     * description of the lookup
     */
    @NonNull
    private String description;
    /*
     * fields description map
     */
    @NonNull
    private Map<String, String> fieldsDescription;
}
