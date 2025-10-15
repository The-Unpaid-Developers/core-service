package com.project.core_service.integration;

import com.project.core_service.models.query.Query;
import com.project.core_service.repositories.QueryRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for
 * {@link com.project.core_service.controllers.QueryController}.
 * 
 * <p>
 * These tests verify the complete end-to-end flow of query operations,
 * including controller layer, service layer, and database persistence.
 * </p>
 * 
 * <h2>Test Scenarios Covered:</h2>
 * <ul>
 * <li>Creating new queries</li>
 * <li>Retrieving queries (by name, all, paginated)</li>
 * <li>Updating queries</li>
 * <li>Deleting queries</li>
 * <li>Error handling and validation</li>
 * </ul>
 */
@DisplayName("Query Controller Integration Tests")
@Epic("Query Management")
@Feature("Query CRUD Operations")
public class QueryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private QueryRepository queryRepository;

    // ==================== CREATE OPERATIONS ====================

    @Nested
    @DisplayName("POST /api/v1/queries - Create Query")
    @Story("Create Query")
    class CreateQueryTests {

        @Test
        @DisplayName("Should create a new query with valid data")
        @Description("Creates a new query with name and query string")
        @Severity(SeverityLevel.CRITICAL)
        void shouldCreateNewQuery() throws Exception {
            // Given
            Query newQuery = Query.builder()
                    .name("getUserByEmail")
                    .query("{\"email\": \"test@example.com\"}")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(newQuery)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("getUserByEmail"))
                    .andExpect(jsonPath("$.query").value("{\"email\": \"test@example.com\"}"));

            // Verify in database
            Optional<Query> saved = queryRepository.findById("getUserByEmail");
            assertThat(saved).isPresent();
            assertThat(saved.get().getQuery()).isEqualTo("{\"email\": \"test@example.com\"}");
        }

        @Test
        @DisplayName("Should fail to create query with duplicate name")
        @Description("Validates that duplicate query names are not allowed")
        @Severity(SeverityLevel.CRITICAL)
        void shouldFailToCreateDuplicateQuery() throws Exception {
            // Given - create first query
            Query firstQuery = Query.builder()
                    .name("getActiveOrders")
                    .query("{\"status\": \"ACTIVE\"}")
                    .build();
            queryRepository.save(firstQuery);

            // When & Then - try to create duplicate
            Query duplicateQuery = Query.builder()
                    .name("getActiveOrders")
                    .query("{\"status\": \"PENDING\"}")
                    .build();

            mockMvc.perform(post("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(duplicateQuery)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("already exists")));
        }

        @Test
        @DisplayName("Should fail with 400 when request body is invalid")
        @Description("Validates request validation for missing required fields")
        @Severity(SeverityLevel.NORMAL)
        void shouldFailWithInvalidRequestBody() throws Exception {
            // Given - query with null query string
            String invalidJson = "{\"name\":\"testQuery\",\"query\":null}";

            // When & Then
            mockMvc.perform(post("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when name is empty")
        @Description("Validates that query name cannot be empty")
        @Severity(SeverityLevel.NORMAL)
        void shouldFailWhenNameIsEmpty() throws Exception {
            // Given
            Query invalidQuery = Query.builder()
                    .name("")
                    .query("{\"test\": \"value\"}")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(invalidQuery)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("cannot be null or empty")));
        }

        @Test
        @DisplayName("Should fail when query contains forbidden operations")
        @Description("Validates that write operations are not allowed")
        @Severity(SeverityLevel.CRITICAL)
        void shouldFailWhenQueryContainsForbiddenOperations() throws Exception {
            // Given
            Query invalidQuery = Query.builder()
                    .name("dangerousQuery")
                    .query("{\"$out\": \"newCollection\"}")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(invalidQuery)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("forbidden operation")));
        }
    }

    // ==================== READ OPERATIONS ====================

    @Nested
    @DisplayName("GET Operations - Retrieve Queries")
    @Story("Retrieve Queries")
    class RetrieveQueryTests {

        @Test
        @DisplayName("Should retrieve query by name")
        @Description("Fetches a specific query using its name")
        @Severity(SeverityLevel.CRITICAL)
        void shouldRetrieveQueryByName() throws Exception {
            // Given
            Query query = Query.builder()
                    .name("getCustomers")
                    .query("SELECT * FROM customers")
                    .build();
            queryRepository.save(query);

            // When & Then
            mockMvc.perform(get("/api/v1/queries/{name}", "getCustomers")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("getCustomers"))
                    .andExpect(jsonPath("$.query").value("SELECT * FROM customers"));
        }

        @Test
        @DisplayName("Should return 404 when query not found")
        @Description("Validates proper error handling when requesting a non-existent query")
        @Severity(SeverityLevel.NORMAL)
        void shouldReturn404WhenNotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/queries/{name}", "nonExistent")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should retrieve all queries")
        @Description("Fetches all queries without pagination")
        @Severity(SeverityLevel.NORMAL)
        void shouldRetrieveAllQueries() throws Exception {
            // Given - create multiple queries
            queryRepository.save(Query.builder()
                    .name("query1")
                    .query("{\"field1\": \"value1\"}")
                    .build());
            queryRepository.save(Query.builder()
                    .name("query2")
                    .query("{\"field2\": \"value2\"}")
                    .build());
            queryRepository.save(Query.builder()
                    .name("query3")
                    .query("{\"field3\": \"value3\"}")
                    .build());

            // When & Then
            mockMvc.perform(get("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[*].name", containsInAnyOrder("query1", "query2", "query3")));
        }

        @Test
        @DisplayName("Should retrieve queries with pagination")
        @Description("Fetches paginated queries with specified page and size")
        @Severity(SeverityLevel.NORMAL)
        void shouldRetrieveQueriesWithPagination() throws Exception {
            // Given - create 5 queries
            for (int i = 1; i <= 5; i++) {
                queryRepository.save(Query.builder()
                        .name("query" + i)
                        .query("{\"field" + i + "\": \"value" + i + "\"}")
                        .build());
            }

            // When & Then - get page 0 with size 2
            mockMvc.perform(get("/api/v1/queries/paging")
                    .param("page", "0")
                    .param("size", "2")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(5))
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        @DisplayName("Should return empty list when no queries exist")
        @Description("Validates response when querying empty collection")
        @Severity(SeverityLevel.NORMAL)
        void shouldReturnEmptyListWhenNoQueries() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ==================== UPDATE OPERATIONS ====================

    @Nested
    @DisplayName("PUT Operations - Update Queries")
    @Story("Update Queries")
    class UpdateQueryTests {

        @Test
        @DisplayName("Should update an existing query")
        @Description("Updates an existing query with new query string")
        @Severity(SeverityLevel.CRITICAL)
        void shouldUpdateQuery() throws Exception {
            // Given
            Query existingQuery = Query.builder()
                    .name("updateTest")
                    .query("{\"active\": false}")
                    .build();
            queryRepository.save(existingQuery);

            // Modify the query
            Query updatedQuery = Query.builder()
                    .name("updateTest")
                    .query("{\"active\": true}")
                    .build();

            // When & Then
            mockMvc.perform(put("/api/v1/queries/{name}", "updateTest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(updatedQuery)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("updateTest"))
                    .andExpect(jsonPath("$.query").value("{\"active\": true}"));

            // Verify in database
            Query updated = queryRepository.findById("updateTest").orElseThrow();
            assertThat(updated.getQuery()).isEqualTo("{\"active\": true}");
        }

        @Test
        @DisplayName("Should fail to update non-existent query")
        @Description("Validates error handling when updating a query that doesn't exist")
        @Severity(SeverityLevel.CRITICAL)
        void shouldFailToUpdateNonExistentQuery() throws Exception {
            // Given
            Query updatedQuery = Query.builder()
                    .name("nonExistent")
                    .query("{\"test\": \"value\"}")
                    .build();

            // When & Then
            mockMvc.perform(put("/api/v1/queries/{name}", "nonExistent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(updatedQuery)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Query not found")));
        }

        @Test
        @DisplayName("Should fail when updated query string is empty")
        @Description("Validates that query string cannot be empty during update")
        @Severity(SeverityLevel.NORMAL)
        void shouldFailWhenUpdatedQueryStringIsEmpty() throws Exception {
            // Given
            Query existingQuery = Query.builder()
                    .name("testQuery")
                    .query("{\"test\": \"value\"}")
                    .build();
            queryRepository.save(existingQuery);

            Query invalidUpdate = Query.builder()
                    .name("testQuery")
                    .query("   ")
                    .build();

            // When & Then
            mockMvc.perform(put("/api/v1/queries/{name}", "testQuery")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(invalidUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("cannot be null or empty")));
        }
    }

    // ==================== DELETE OPERATIONS ====================

    @Nested
    @DisplayName("DELETE /api/v1/queries/{name} - Delete Query")
    @Story("Delete Query")
    class DeleteQueryTests {

        @Test
        @DisplayName("Should delete a query")
        @Description("Deletes an existing query")
        @Severity(SeverityLevel.CRITICAL)
        void shouldDeleteQuery() throws Exception {
            // Given
            Query query = Query.builder()
                    .name("deleteTest")
                    .query("{\"temp\": true}")
                    .build();
            queryRepository.save(query);

            // When & Then
            mockMvc.perform(delete("/api/v1/queries/{name}", "deleteTest")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // Verify deletion
            Optional<Query> deleted = queryRepository.findById("deleteTest");
            assertThat(deleted).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent query")
        @Description("Validates error handling when attempting to delete a non-existent query")
        @Severity(SeverityLevel.NORMAL)
        void shouldReturn404WhenDeletingNonExistentQuery() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/v1/queries/{name}", "nonExistent")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Query not found")));
        }
    }

    // ==================== COMPLEX SCENARIOS ====================

    @Nested
    @DisplayName("Complex Scenarios")
    @Story("Complex Query Operations")
    class ComplexScenarioTests {

        @Test
        @DisplayName("Should handle complete CRUD lifecycle")
        @Description("Creates, reads, updates, and deletes a query in sequence")
        @Severity(SeverityLevel.CRITICAL)
        void shouldHandleCompleteCRUDLifecycle() throws Exception {
            String queryName = "lifecycleTest";

            // Create
            Query newQuery = Query.builder()
                    .name(queryName)
                    .query("{\"active\": true}")
                    .build();

            mockMvc.perform(post("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(newQuery)))
                    .andExpect(status().isCreated());

            // Read
            mockMvc.perform(get("/api/v1/queries/{name}", queryName)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(queryName));

            // Update
            Query updatedQuery = Query.builder()
                    .name(queryName)
                    .query("{\"active\": true, \"verified\": true}")
                    .build();

            mockMvc.perform(put("/api/v1/queries/{name}", queryName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(updatedQuery)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.query").value("{\"active\": true, \"verified\": true}"));

            // Delete
            mockMvc.perform(delete("/api/v1/queries/{name}", queryName)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // Verify deletion
            mockMvc.perform(get("/api/v1/queries/{name}", queryName)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle multiple concurrent queries")
        @Description("Tests system behavior with multiple queries")
        @Severity(SeverityLevel.NORMAL)
        void shouldHandleMultipleQueries() throws Exception {
            // Create multiple queries with properly formatted JSON
            for (int i = 1; i <= 10; i++) {
                Query query = Query.builder()
                        .name("query" + i)
                        .query(String.format("{\"field%d\": \"value%d\"}", i, i))
                        .build();
                queryRepository.save(query);
            }

            // Verify all exist
            mockMvc.perform(get("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(10)));

            // Update one - make sure it's valid JSON
            Query updatedQuery = Query.builder()
                    .name("query5")
                    .query("{\"updated\": true}")
                    .build();

            mockMvc.perform(put("/api/v1/queries/{name}", "query5")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(updatedQuery)))
                    .andExpect(status().isOk());

            // Delete one
            mockMvc.perform(delete("/api/v1/queries/{name}", "query10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // Verify count
            mockMvc.perform(get("/api/v1/queries")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(9)));
        }
    }

    // ==================== EXECUTE OPERATIONS ====================

    @Nested
    @DisplayName("POST /api/v1/queries/{name}/execute - Execute Query")
    @Story("Execute Query")
    class ExecuteQueryTests {

        @Test
        @DisplayName("Should execute a stored query successfully")
        @Description("Executes a stored query against a MongoDB collection")
        @Severity(SeverityLevel.CRITICAL)
        void shouldExecuteQuerySuccessfully() throws Exception {
            // Given - save a query
            Query storedQuery = Query.builder()
                    .name("findActiveUsers")
                    .query("{\"active\": true}")
                    .build();
            queryRepository.save(storedQuery);

            // Create test data in MongoDB
            mongoTemplate.save(org.bson.Document.parse("{\"name\": \"John\", \"active\": true}"), "users");
            mongoTemplate.save(org.bson.Document.parse("{\"name\": \"Jane\", \"active\": true}"), "users");
            mongoTemplate.save(org.bson.Document.parse("{\"name\": \"Bob\", \"active\": false}"), "users");

            // When & Then
            String requestBody = "{\"collection\": \"users\", \"limit\": 10}";

            mockMvc.perform(post("/api/v1/queries/{name}/execute", "findActiveUsers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].active", everyItem(is(true))));
        }

        @Test
        @DisplayName("Should execute query with specific MongoDB conditions")
        @Description("Executes a query with pre-defined MongoDB conditions in the stored query")
        @Severity(SeverityLevel.NORMAL)
        void shouldExecuteQueryWithSpecificConditions() throws Exception {
            // Given
            Query storedQuery = Query.builder()
                    .name("findAdultUsers")
                    .query("{\"age\": {\"$gte\": 18}}")
                    .build();
            queryRepository.save(storedQuery);

            // Create test data
            mongoTemplate.save(org.bson.Document.parse("{\"name\": \"John\", \"age\": 25}"), "users");
            mongoTemplate.save(org.bson.Document.parse("{\"name\": \"Jane\", \"age\": 17}"), "users");
            mongoTemplate.save(org.bson.Document.parse("{\"name\": \"Bob\", \"age\": 30}"), "users");

            // When & Then - query should return only users >= 18 years old
            String requestBody = "{\"collection\": \"users\", \"limit\": 10}";

            mockMvc.perform(post("/api/v1/queries/{name}/execute", "findAdultUsers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Should apply limit and skip parameters")
        @Description("Tests pagination with limit and skip")
        @Severity(SeverityLevel.NORMAL)
        void shouldApplyLimitAndSkip() throws Exception {
            // Given
            Query storedQuery = Query.builder()
                    .name("getAllUsers")
                    .query("{}")
                    .build();
            queryRepository.save(storedQuery);

            // Create 10 test documents
            for (int i = 1; i <= 10; i++) {
                mongoTemplate.save(org.bson.Document.parse("{\"name\": \"User" + i + "\"}"), "users");
            }

            // When & Then - get 3 items starting from index 5
            String requestBody = "{\"collection\": \"users\", \"limit\": 3, \"skip\": 5}";

            mockMvc.perform(post("/api/v1/queries/{name}/execute", "getAllUsers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        @DisplayName("Should fail when query not found")
        @Description("Returns 404 when trying to execute non-existent query")
        @Severity(SeverityLevel.NORMAL)
        void shouldFailWhenQueryNotFound() throws Exception {
            // When & Then
            String requestBody = "{\"collection\": \"users\"}";

            mockMvc.perform(post("/api/v1/queries/{name}/execute", "nonExistent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Query not found")));
        }

        @Test
        @DisplayName("Should fail when collection not specified")
        @Description("Returns 400 when collection name is missing")
        @Severity(SeverityLevel.NORMAL)
        void shouldFailWhenCollectionNotSpecified() throws Exception {
            // Given
            Query storedQuery = Query.builder()
                    .name("testQuery")
                    .query("{\"test\": true}")
                    .build();
            queryRepository.save(storedQuery);

            // When & Then
            String requestBody = "{}";

            mockMvc.perform(post("/api/v1/queries/{name}/execute", "testQuery")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Collection name must be specified")));
        }

        @Test
        @DisplayName("Should return empty list when no documents match")
        @Description("Executes query that matches no documents")
        @Severity(SeverityLevel.NORMAL)
        void shouldReturnEmptyListWhenNoMatches() throws Exception {
            // Given
            Query storedQuery = Query.builder()
                    .name("findNonExistent")
                    .query("{\"nonExistentField\": \"value\"}")
                    .build();
            queryRepository.save(storedQuery);

            // When & Then
            String requestBody = "{\"collection\": \"users\", \"limit\": 10}";

            mockMvc.perform(post("/api/v1/queries/{name}/execute", "findNonExistent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
