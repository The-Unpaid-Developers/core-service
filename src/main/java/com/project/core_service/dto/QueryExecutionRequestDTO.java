package com.project.core_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for query execution parameters.
 *
 * <p>
 * This DTO allows users to provide additional filters and conditions
 * when executing a stored query.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryExecutionRequestDTO {

    /**
     * The collection to query against.
     * If not provided, it will default to the solutionReviews collection. 
     */
    private String collection;

    /**
     * Maximum number of documents to return.
     * Default is 100 if not specified.
     */
    private Integer limit;

    /**
     * Number of documents to skip.
     * Used for pagination.
     */
    private Integer skip;
}
