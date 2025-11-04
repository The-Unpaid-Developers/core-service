package com.project.core_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.mongodb.lang.NonNull;

/**
 * DTO for creating a new query.
 * 
 * <p>
 * This DTO is used to accept client requests for creating queries,
 * preventing direct manipulation of the Query entity properties.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateQueryRequestDTO {

    /**
     * The unique name of the query.
     */
    @NonNull
    private String name;

    /**
     * The actual database query string.
     */
    @NonNull
    private String mongoQuery;

    /**
     * A brief description of the query's purpose.
     */
    @NonNull
    private String description;
}
