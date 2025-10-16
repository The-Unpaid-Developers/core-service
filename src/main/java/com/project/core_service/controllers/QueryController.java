package com.project.core_service.controllers;

import com.project.core_service.dto.CreateQueryRequestDTO;
import com.project.core_service.dto.QueryExecutionRequestDTO;
import com.project.core_service.dto.UpdateQueryRequestDTO;
import com.project.core_service.models.query.Query;
import com.project.core_service.services.QueryService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing {@link Query} resources.
 * 
 * <p>
 * Provides endpoints for creating, retrieving, updating, and deleting
 * queries. Supports pagination for listing queries.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/queries")
public class QueryController {

    private final QueryService queryService;

    @Autowired
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Retrieves a {@link Query} by its name.
     * 
     * @param name the name of the query
     * @return a {@link ResponseEntity} containing the query if found,
     *         or {@code 404 Not Found} if no query exists for the given name
     */
    @GetMapping("/{name}")
    public ResponseEntity<Query> getQueryByName(@PathVariable String name) {
        return queryService.getQueryByName(name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all {@link Query} entries.
     * 
     * @return a {@link ResponseEntity} containing a list of all queries
     */
    @GetMapping
    public ResponseEntity<List<Query>> getAllQueries() {
        return ResponseEntity.ok(queryService.getAllQueries());
    }

    /**
     * Retrieves all {@link Query} entries with pagination.
     * 
     * @param page the page index (0-based)
     * @param size the number of items per page
     * @return a {@link ResponseEntity} containing a paginated list of queries
     */
    @GetMapping("/paging")
    public ResponseEntity<Page<Query>> getQueries(
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(queryService.getQueries(pageable));
    }

    /**
     * Creates a new {@link Query}.
     * 
     * @param request the DTO containing query creation data
     * @return a {@link ResponseEntity} containing the created query
     *         with status {@code 201 Created}
     */
    @PostMapping
    public ResponseEntity<Query> createMongoQuery(@RequestBody CreateQueryRequestDTO request) {
        return new ResponseEntity<>(queryService.createMongoQuery(request), HttpStatus.CREATED);
    }

    /**
     * Updates an existing {@link Query}.
     * 
     * @param name    the name of the query to update
     * @param request the DTO containing updated query data
     * @return a {@link ResponseEntity} containing the updated query
     */
    @PutMapping("/{name}")
    public ResponseEntity<Query> updateMongoQuery(
            @PathVariable String name,
            @RequestBody UpdateQueryRequestDTO request) {
        return ResponseEntity.ok(queryService.updateMongoQuery(name, request));
    }

    /**
     * Deletes a {@link Query} by its name.
     *
     * @param name the name of the query to delete
     * @return a {@link ResponseEntity} with status {@code 204 No Content}
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteMongoQuery(@PathVariable String name) {
        queryService.deleteMongoQuery(name);
        return ResponseEntity.noContent().build();
    }

    /**
     * Executes a stored query by name with limit and skip parameters.
     *
     * <p>
     * This endpoint executes a stored MongoDB read query against the database.
     * Only GET/read operations are allowed. The query can be augmented with
     * limit, and skip parameters.
     * </p>
     *
     * @param name    the name of the query to execute
     * @param request the execution parameters including collection, limit, and skip
     *                parameters
     * @return a {@link ResponseEntity} containing the list of documents matching
     *         the query
     */
    @PostMapping("/{name}/execute")
    public ResponseEntity<List<Document>> executeMongoQuery(
            @PathVariable String name,
            @RequestBody QueryExecutionRequestDTO request) {
        List<Document> results = queryService.executeMongoQuery(name, request);
        return ResponseEntity.ok(results);
    }
}
