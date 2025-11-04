package com.project.core_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.mongodb.lang.NonNull;

/**
 * DTO for updating an existing query.
 * 
 * <p>
 * This DTO is used to accept client requests for updating queries,
 * preventing direct manipulation of the Query entity properties.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateQueryRequestDTO {

    /**
     * The updated database query string.
     */
    @NonNull
    private String mongoQuery;

    /**
     * The updated description of the query's purpose if any.
     */
    private String description;
}
