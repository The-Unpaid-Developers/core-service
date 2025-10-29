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
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    private static final String COLLECTION_FIELD = "collection";

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
     * Executes a stored aggregation pipeline by name with optional parameters.
     *
     * @param name    the name of the query to execute
     * @param request the execution parameters (collection, limit, skip)
     * @return a list of documents matching the aggregation pipeline
     * @throws NotFoundException        if no query exists with the given name
     * @throws IllegalArgumentException if the query or parameters are invalid
     */
    public List<Document> executeMongoQuery(String name, QueryExecutionRequestDTO request) {
        // Retrieve the stored query
        Query storedQuery = queryRepository.findById(name)
                .orElseThrow(() -> new NotFoundException("Query not found with name: " + name));

        try {
            // Parse the stored query as a MongoDB aggregation pipeline
            // Expected format: [{"$match": {...}}, {"$group": {...}}, ...]
            String queryString = storedQuery.getMongoQuery().trim();

            // Validate it's a JSON array
            if (!queryString.startsWith("[")) {
                throw new IllegalArgumentException(
                        "MongoDB aggregation pipeline must be a JSON array starting with '['");
            }

            // Parse as a list of pipeline stages
            @SuppressWarnings("unchecked")
            List<Document> pipelineStages = Document.parse("{\"stages\": " + queryString + "}")
                    .getList("stages", Document.class);

            if (pipelineStages == null || pipelineStages.isEmpty()) {
                throw new IllegalArgumentException("Aggregation pipeline cannot be empty");
            }

            // Extract collection name
            String collection = request.getCollection();
            if (collection == null || collection.trim().isEmpty()) {
                throw new IllegalArgumentException("Collection name must be specified in the request");
            }

            // Add $limit stage if specified (default to 100)
            int limit = request.getLimit() != null ? request.getLimit() : 100;

            // Add $skip stage if specified
            List<Document> modifiedPipeline = new ArrayList<>(pipelineStages);
            if (request.getSkip() != null && request.getSkip() > 0) {
                modifiedPipeline.add(new Document("$skip", request.getSkip()));
            }
            modifiedPipeline.add(new Document("$limit", limit));

            // Build and execute the aggregation
            List<org.springframework.data.mongodb.core.aggregation.AggregationOperation> operations =
                    new ArrayList<>();

            for (Document stage : modifiedPipeline) {
                operations.add(context -> stage);
            }

            Aggregation aggregation = Aggregation.newAggregation(operations);

            AggregationResults<Document> results = mongoTemplate.aggregate(
                    aggregation, collection, Document.class);

            return results.getMappedResults();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to execute aggregation pipeline: " + e.getMessage(), e);
        }
    }

    /**
     * Validates that an aggregation pipeline is read-only.
     *
     * <p>
     * This method checks that the pipeline does not contain write operations
     * such as $out, $merge, or other dangerous operations. It allows standard
     * read-only aggregation stages like $match, $group, $project, $lookup, etc.
     * </p>
     *
     * @param queryString the aggregation pipeline string to validate
     * @throws IllegalArgumentException if the pipeline contains write operations
     */
    private void validateReadOnlyQuery(String queryString) {
        if (queryString == null || queryString.trim().isEmpty()) {
            throw new IllegalArgumentException("Query string cannot be null or empty");
        }

        String trimmedQuery = queryString.trim();

        // Validate that it's valid JSON (either array or object)
        try {
            if (trimmedQuery.startsWith("[")) {
                // Parse as array
                Document.parse("{\"pipeline\": " + trimmedQuery + "}");
            } else {
                // Parse as document
                Document.parse(trimmedQuery);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Query must be valid JSON format: " + e.getMessage(), e);
        }

        String lowerQuery = queryString.toLowerCase();

        // List of forbidden MongoDB operators and operations
        // These are write operations that should not be allowed
        String[] forbiddenOperators = {
                "\"$out\"", "'$out'", // Aggregation output stage (writes to collection)
                "\"$merge\"", "'$merge'", // Aggregation merge stage (writes to collection)
                "insertone", "insertmany", "insert(", // Insert operations
                "deleteone", "deletemany", "delete(", "remove(", // Delete operations
                "updateone", "updatemany", "update(", "findandmodify", // Update operations
                "replaceone", "replace(", // Replace operations
                "drop(", "dropdatabase", "dropcollection", // Drop operations
                "create(", "createcollection", "createindex", // Create operations
                "$eval", "function(", "function ", // Code execution
                "mapreduce", "map-reduce", // MapReduce operations
        };

        for (String forbidden : forbiddenOperators) {
            if (lowerQuery.contains(forbidden)) {
                throw new IllegalArgumentException(
                        "Query contains forbidden operation: " + forbidden + ". Only read operations are allowed.");
            }
        }
    }
}
