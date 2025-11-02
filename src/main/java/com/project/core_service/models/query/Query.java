package com.project.core_service.models.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

/**
 * Represents a database query entity.
 * 
 * <p>
 * This entity stores reusable database queries with a unique name identifier
 * and the actual query string. The name serves as the primary key.
 * </p>
 */
@Document(collection = "queries")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Query {

    /**
     * The unique name of the query, used as the primary key.
     */
    @Id
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
