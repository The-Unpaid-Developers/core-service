package com.project.core_service.services;

import com.project.core_service.dto.CreateQueryRequestDTO;
import com.project.core_service.dto.QueryExecutionRequestDTO;
import com.project.core_service.dto.UpdateQueryRequestDTO;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.query.Query;
import com.project.core_service.repositories.QueryRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing {@link Query} entities.
 * 
 * <p>
 * This service provides CRUD operations for queries, as well as
 * support for pagination. It acts as the business logic layer between
 * the controller and repository.
 * </p>
 */
@Service
public class QueryService {

    private final QueryRepository queryRepository;
    private final MongoTemplate mongoTemplate;

    private final String COLLECTION = "collection";

    @Autowired
    public QueryService(QueryRepository queryRepository, MongoTemplate mongoTemplate) {
        this.queryRepository = queryRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Retrieves a {@link Query} by its name (primary key).
     * 
     * @param name the name of the query
     * @return an {@link Optional} containing the query if found,
     *         or empty if no query exists for the given name
     */
    public Optional<Query> getQueryByName(String name) {
        return queryRepository.findById(name);
    }

    /**
     * Retrieves all {@link Query} entries.
     * 
     * @return a {@link List} of all queries
     */
    public List<Query> getAllQueries() {
        return queryRepository.findAll();
    }

    /**
     * Retrieves all {@link Query} entries with pagination.
     * 
     * @param pageable the pagination information
     * @return a {@link Page} of queries
     */
    public Page<Query> getQueries(Pageable pageable) {
        return queryRepository.findAll(pageable);
    }

    /**
     * Creates a new {@link Query}.
     * 
     * @param request the DTO containing query creation data
     * @return the newly created query
     * @throws IllegalArgumentException if request is null or name already exists
     */
    public Query createMongoQuery(CreateQueryRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Query request cannot be null");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Query name cannot be null or empty");
        }

        if (request.getMongoQuery() == null || request.getMongoQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Query string cannot be null or empty");
        }

        // Validate that the query is a read-only MongoDB query
        validateReadOnlyQuery(request.getMongoQuery());

        // Check if query with this name already exists
        if (queryRepository.existsById(request.getName())) {
            throw new IllegalArgumentException("Query with name '" + request.getName() + "' already exists");
        }

        // Map DTO to entity
        Query query = Query.builder()
                .name(request.getName())
                .mongoQuery(request.getMongoQuery())
                .build();

        return queryRepository.save(query);
    }

    /**
     * Updates an existing {@link Query}.
     * 
     * <p>
     * If the query does not exist, a {@link NotFoundException} is thrown.
     * </p>
     * 
     * @param name    the name of the query to update
     * @param request the DTO containing updated query data
     * @return the updated query
     * @throws NotFoundException        if no query exists with the given name
     * @throws IllegalArgumentException if updated query data is invalid
     */
    public Query updateMongoQuery(String name, UpdateQueryRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Updated query request cannot be null");
        }

        if (request.getMongoQuery() == null || request.getMongoQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Query string cannot be null or empty");
        }

        // Validate that the query is a read-only MongoDB query
        validateReadOnlyQuery(request.getMongoQuery());

        // Check if query exists
        Query existingQuery = queryRepository.findById(name)
                .orElseThrow(() -> new NotFoundException("Query not found with name: " + name));

        // Update the query string
        existingQuery.setMongoQuery(request.getMongoQuery());

        return queryRepository.save(existingQuery);
    }

    /**
     * Deletes a {@link Query} by its name.
     * 
     * @param name the name of the query to delete
     * @throws NotFoundException if no query exists with the given name
     */
    public void deleteMongoQuery(String name) {
        if (!queryRepository.existsById(name)) {
            throw new NotFoundException("Query not found with name: " + name);
        }

        queryRepository.deleteById(name);
    }

    /**
     * Executes a stored query by name with optional filters and conditions.
     *
     * @param name    the name of the query to execute
     * @param request the execution parameters (filters, limit, skip)
     * @return a list of documents matching the query
     * @throws NotFoundException        if no query exists with the given name
     * @throws IllegalArgumentException if the query or parameters are invalid
     */
    public List<Document> executeMongoQuery(String name, QueryExecutionRequestDTO request) {
        // Retrieve the stored query
        Query storedQuery = queryRepository.findById(name)
                .orElseThrow(() -> new NotFoundException("Query not found with name: " + name));

        try {
            // Parse the stored query as a MongoDB query document
            Document queryDoc = Document.parse(storedQuery.getMongoQuery());

            // Extract collection name
            String collection = request.getCollection();
            if (collection == null || collection.trim().isEmpty()) {
                // Try to extract from query if it contains a collection field
                if (queryDoc.containsKey(COLLECTION)) {
                    collection = queryDoc.getString(COLLECTION);
                    queryDoc.remove(COLLECTION);
                } else {
                    throw new IllegalArgumentException(
                            "Collection name must be specified either in the request or in the stored query");
                }
            }

            // Create BasicQuery with limit and skip
            BasicQuery mongoQuery = new BasicQuery(queryDoc.toJson());

            // Apply limit (default to 100 if not specified)
            int limit = request.getLimit() != null ? request.getLimit() : 100;
            mongoQuery.limit(limit);

            // Apply skip if specified
            if (request.getSkip() != null && request.getSkip() > 0) {
                mongoQuery.skip(request.getSkip());
            }

            // Execute the query
            return mongoTemplate.find(mongoQuery, Document.class, collection);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    /**
     * Validates that a query string is a read-only MongoDB query.
     *
     * <p>
     * This method checks that the query does not contain write operations
     * such as insert, update, delete, or other dangerous operations.
     * </p>
     *
     * @param queryString the query string to validate
     * @throws IllegalArgumentException if the query contains write operations
     */
    private void validateReadOnlyQuery(String queryString) {
        if (queryString == null || queryString.trim().isEmpty()) {
            throw new IllegalArgumentException("Query string cannot be null or empty");
        }

        // Validate that it's valid JSON first
        try {
            Document.parse(queryString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Query must be valid JSON format: " + e.getMessage(), e);
        }

        String lowerQuery = queryString.toLowerCase();

        // List of forbidden MongoDB operators and operations
        // These are MongoDB-specific operators that perform write operations
        String[] forbiddenOperators = {
                "\"$out\"", "\"$merge\"", // Aggregation output stages
                "insertone", "insertmany", "insert(", // Insert operations
                "deleteone", "deletemany", "delete(", "remove(", // Delete operations
                "updateone", "updatemany", "update(", "findandmodify", // Update operations
                "replaceone", "replace(", // Replace operations
                "drop(", "dropdatabase", "dropcollection", // Drop operations
                "create(", "createcollection", "createindex", // Create operations
                "$eval", "function(", "function ", // Code execution
        };

        for (String forbidden : forbiddenOperators) {
            if (lowerQuery.contains(forbidden)) {
                throw new IllegalArgumentException(
                        "Query contains forbidden operation: " + forbidden + ". Only read operations are allowed.");
            }
        }
    }
}
