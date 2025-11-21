package com.project.core_service.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.project.core_service.dto.CreateQueryRequestDTO;
import com.project.core_service.dto.QueryExecutionRequestDTO;
import com.project.core_service.dto.UpdateQueryRequestDTO;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.query.Query;
import com.project.core_service.repositories.QueryRepository;

/**
 * Unit tests for {@link QueryService}.
 */
@ExtendWith(MockitoExtension.class)
class QueryServiceTest {

    @Mock
    private QueryRepository queryRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private QueryService queryService;

    private Query testQuery;

    @BeforeEach
    void setup() {
        testQuery = Query.builder()
                .name("getUserByEmail")
                .mongoQuery("[{\"$match\": {\"email\": \"test@example.com\"}}]")
                .description("Retrieve user by email")
                .build();
    }

    @Test
    void getQueryByName_Found() {
        when(queryRepository.findById("getUserByEmail")).thenReturn(Optional.of(testQuery));

        Optional<Query> result = queryService.getQueryByName("getUserByEmail");

        assertTrue(result.isPresent());
        assertEquals("getUserByEmail", result.get().getName());
        assertEquals("[{\"$match\": {\"email\": \"test@example.com\"}}]", result.get().getMongoQuery());
        assertEquals("Retrieve user by email", result.get().getDescription());
        verify(queryRepository).findById("getUserByEmail");
    }

    @Test
    void getQueryByName_NotFound() {
        when(queryRepository.findById("nonExistent")).thenReturn(Optional.empty());

        Optional<Query> result = queryService.getQueryByName("nonExistent");

        assertTrue(result.isEmpty());
        verify(queryRepository).findById("nonExistent");
    }

    @Test
    void getAllQueries() {
        Query query2 = Query.builder()
                .name("getActiveOrders")
                .mongoQuery("[{\"$match\": {\"status\": \"ACTIVE\"}}]")
                .description("Retrieve all active orders")
                .build();

        when(queryRepository.findAll()).thenReturn(List.of(testQuery, query2));

        List<Query> result = queryService.getAllQueries();

        assertEquals(2, result.size());
        assertTrue(result.contains(testQuery));
        assertTrue(result.contains(query2));
        verify(queryRepository).findAll();
    }

    @Test
    void getQueries_Page() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Query> page = new PageImpl<>(List.of(testQuery));

        when(queryRepository.findAll(pageable)).thenReturn(page);

        Page<Query> result = queryService.getQueries(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(testQuery, result.getContent().get(0));
        verify(queryRepository).findAll(pageable);
    }

    @Test
    void createMongoQuery_Success() {
        CreateQueryRequestDTO request = CreateQueryRequestDTO.builder()
                .name("getUserByEmail")
                .mongoQuery("[{\"$match\": {\"email\": \"test@example.com\"}}]")
                .description("Retrieve user by email")
                .build();

        when(queryRepository.existsById("getUserByEmail")).thenReturn(false);
        when(queryRepository.save(any(Query.class))).thenReturn(testQuery);

        Query result = queryService.createMongoQuery(request);

        assertNotNull(result);
        assertEquals("getUserByEmail", result.getName());
        assertEquals("[{\"$match\": {\"email\": \"test@example.com\"}}]", result.getMongoQuery());
        assertEquals("Retrieve user by email", result.getDescription());
        verify(queryRepository).existsById("getUserByEmail");
        verify(queryRepository).save(any(Query.class));
    }

    @Test
    void createMongoQuery_ThrowsWhenNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createMongoQuery(null));

        assertEquals("Query request cannot be null", exception.getMessage());
        verify(queryRepository, never()).save(any());
    }

    @Test
    void createMongoQuery_ThrowsWhenNameIsEmpty() {
        CreateQueryRequestDTO invalidRequest = CreateQueryRequestDTO.builder()
                .name("   ")
                .mongoQuery("[{\"$match\": {\"test\": \"value\"}}]")
                .description("test")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createMongoQuery(invalidRequest));

        assertEquals("Query name cannot be null or empty", exception.getMessage());
        verify(queryRepository, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("invalidMongoQueryTestCases")
    void createMongoQuery_ThrowsForInvalidQueries(String name, String mongoQuery, String description, String expectedMessageFragment) {
        CreateQueryRequestDTO invalidRequest = CreateQueryRequestDTO.builder()
                .name(name)
                .mongoQuery(mongoQuery)
                .description(description)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createMongoQuery(invalidRequest));

        assertTrue(exception.getMessage().contains(expectedMessageFragment));
        verify(queryRepository, never()).save(any());
    }

    static Stream<Arguments> invalidMongoQueryTestCases() {
        return Stream.of(
                Arguments.of("dangerousQuery", "[{\"$out\": \"newCollection\"}]", "forbidden operation", "forbidden operation"),
                Arguments.of("invalidQuery1", "this is not valid array of jsons", "invalid query format", "valid format"),
                Arguments.of("invalidQuery2", "{\"$match\": {\"email\": \"test@example.com\"}}", "invalid query format", "valid format"),
                Arguments.of("testQuery", "   ", "query null or empty", "cannot be null or empty"),
                Arguments.of("testDesc", "[{\"$match\": {\"email\": \"test@example.com\"}}]", " ", "cannot be null or empty"));
    }

    @Test
    void createMongoQuery_ThrowsWhenNameAlreadyExists() {
        CreateQueryRequestDTO request = CreateQueryRequestDTO.builder()
                .name("getUserByEmail")
                .mongoQuery("[{\"$match\": {\"email\": \"test@example.com\"}}]")
                .description("Retrieve user by email")
                .build();

        when(queryRepository.existsById("getUserByEmail")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createMongoQuery(request));

        assertEquals("Query with name 'getUserByEmail' already exists", exception.getMessage());
        verify(queryRepository).existsById("getUserByEmail");
        verify(queryRepository, never()).save(any());
    }

    @Test
    void updateMongoQuery_Success() {
        UpdateQueryRequestDTO request = UpdateQueryRequestDTO.builder()
                .mongoQuery("[{\"$match\": {\"email\": \"test@example.com\", \"active\": true}}]")
                .description("Updated description")
                .build();

        Query updatedQuery = Query.builder()
                .name("getUserByEmail")
                .mongoQuery("[{\"$match\": {\"email\": \"test@example.com\", \"active\": true}}]")
                .description("Updated description")
                .build();

        when(queryRepository.findById("getUserByEmail")).thenReturn(Optional.of(testQuery));
        when(queryRepository.save(any(Query.class))).thenReturn(updatedQuery);

        Query result = queryService.updateMongoQuery("getUserByEmail", request);

        assertNotNull(result);
        assertEquals("[{\"$match\": {\"email\": \"test@example.com\", \"active\": true}}]", result.getMongoQuery());
        assertEquals("Updated description", result.getDescription());
        verify(queryRepository).findById("getUserByEmail");
        verify(queryRepository).save(any(Query.class));
    }

    @Test
    void updateMongoQuery_ThrowsWhenNotFound() {
        UpdateQueryRequestDTO request = UpdateQueryRequestDTO.builder()
                .mongoQuery("[{\"$match\": {\"test\": \"value\"}}]")
                .description("Updated description")
                .build();

        when(queryRepository.findById("nonExistent")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> queryService.updateMongoQuery("nonExistent", request));

        assertEquals("Query not found with name: nonExistent", exception.getMessage());
        verify(queryRepository).findById("nonExistent");
        verify(queryRepository, never()).save(any());
    }

    @Test
    void updateMongoQuery_ThrowsWhenUpdatedQueryIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.updateMongoQuery("getUserByEmail", null));

        assertEquals("Updated query request cannot be null", exception.getMessage());
        verify(queryRepository, never()).findById(any());
        verify(queryRepository, never()).save(any());
    }

    @Test
    void updateMongoQuery_ThrowsWhenQueryStringIsEmpty() {
        UpdateQueryRequestDTO invalidRequest = UpdateQueryRequestDTO.builder()
                .mongoQuery("   ")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.updateMongoQuery("getUserByEmail", invalidRequest));

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
        verify(queryRepository, never()).findById(any());
        verify(queryRepository, never()).save(any());
    }

    @Test
    void executeMongoQuery_Success() {
        // Given
        Query storedQuery = Query.builder()
                .name("findActiveUsers")
                .mongoQuery("[{\"$match\": {\"active\": true}}]")
                .description("Find all active users")
                .build();

        QueryExecutionRequestDTO request = QueryExecutionRequestDTO.builder()
                .collection("users")
                .limit(10)
                .build();

        Document doc1 = new Document("name", "John").append("active", true);
        Document doc2 = new Document("name", "Jane").append("active", true);
        List<Document> expectedResults = List.of(doc1, doc2);

        // Mock AggregationResults
        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockResults = (AggregationResults<Document>) org.mockito.Mockito.mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(expectedResults);

        when(queryRepository.findById("findActiveUsers")).thenReturn(Optional.of(storedQuery));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("users"), eq(Document.class)))
                .thenReturn(mockResults);

        // When
        List<Document> results = queryService.executeMongoQuery("findActiveUsers", request);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(queryRepository).findById("findActiveUsers");
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("users"), eq(Document.class));
    }

    @Test
    void executeMongoQuery_ThrowsWhenQueryNotFound() {
        // Given
        QueryExecutionRequestDTO request = QueryExecutionRequestDTO.builder()
                .collection("users")
                .build();

        when(queryRepository.findById("nonExistent")).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> queryService.executeMongoQuery("nonExistent", request));

        assertEquals("Query not found with name: nonExistent", exception.getMessage());
    }

    @Test
    void executeMongoQuery_ThrowsWhenCollectionNotSpecified() {
        // Given
        Query storedQuery = Query.builder()
                .name("testQuery")
                .mongoQuery("[{\"$match\": {\"test\": \"value\"}}]")
                .description("Test query")
                .build();

        QueryExecutionRequestDTO request = QueryExecutionRequestDTO.builder()
                .build();

        when(queryRepository.findById("testQuery")).thenReturn(Optional.of(storedQuery));

        // When & Then - The service now uses a default collection, so this test should verify it uses the default
        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockResults = (AggregationResults<Document>) org.mockito.Mockito.mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(List.of());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class)))
                .thenReturn(mockResults);

        List<Document> results = queryService.executeMongoQuery("testQuery", request);

        assertNotNull(results);
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class));
    }

    @Test
    void executeMongoQuery_ThrowsWhenPipelineNotArray() {
        // Given - A query stored as an object instead of array (valid JSON but not a pipeline)
        Query storedQuery = Query.builder()
                .name("invalidPipeline")
                .mongoQuery("{\"$match\": {\"active\": true}}")
                .description("Invalid pipeline format")
                .build();

        QueryExecutionRequestDTO request = QueryExecutionRequestDTO.builder()
                .collection("users")
                .build();

        when(queryRepository.findById("invalidPipeline")).thenReturn(Optional.of(storedQuery));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.executeMongoQuery("invalidPipeline", request));

        assertTrue(exception.getMessage().contains("must be a JSON array"));
    }

    @Test
    void deleteMongoQuery_Success() {
        when(queryRepository.existsById("getUserByEmail")).thenReturn(true);
        doNothing().when(queryRepository).deleteById("getUserByEmail");

        assertDoesNotThrow(() -> queryService.deleteMongoQuery("getUserByEmail"));

        verify(queryRepository).existsById("getUserByEmail");
        verify(queryRepository).deleteById("getUserByEmail");
    }

    @Test
    void deleteMongoQuery_ThrowsWhenNotFound() {
        when(queryRepository.existsById("nonExistent")).thenReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> queryService.deleteMongoQuery("nonExistent"));

        assertEquals("Query not found with name: nonExistent", exception.getMessage());
        verify(queryRepository).existsById("nonExistent");
        verify(queryRepository, never()).deleteById(any());
    }

    // ==================== EXECUTE MONGO QUERY WITH LIST<MAP> TESTS ====================

    @Test
    void executeMongoQueryWithList_Success() {
        // Given
        List<Map<String, Object>> mongoQuery = List.of(
                Map.of("$match", Map.of("documentState", "ACTIVE")),
                Map.of("$project", Map.of("_id", 1))
        );

        Document doc1 = new Document("_id", "rev-1");
        Document doc2 = new Document("_id", "rev-2");
        List<Document> expectedResults = List.of(doc1, doc2);

        // Mock AggregationResults
        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockResults = (AggregationResults<Document>) org.mockito.Mockito.mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(expectedResults);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class)))
                .thenReturn(mockResults);

        // When
        List<Document> results = queryService.executeMongoQuery(mongoQuery);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("rev-1", results.get(0).get("_id"));
        assertEquals("rev-2", results.get(1).get("_id"));
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class));
    }

    @Test
    void executeMongoQueryWithList_ThrowsWhenNullQuery() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.executeMongoQuery(null));

        assertTrue(exception.getMessage().contains("Aggregation pipeline cannot be empty"));
    }

    @Test
    void executeMongoQueryWithList_ThrowsWhenEmptyQuery() {
        // When & Then
        List<Map<String, Object>> list = List.of();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.executeMongoQuery(list));

        assertTrue(exception.getMessage().contains("Aggregation pipeline cannot be empty"));
    }

    @Test
    void executeMongoQueryWithList_ShouldHandleSingleStage() {
        // Given
        List<Map<String, Object>> mongoQuery = List.of(
                Map.of("$match", Map.of("systemCode", "SYS-123"))
        );

        Document doc1 = new Document("_id", "rev-1");
        List<Document> expectedResults = List.of(doc1);

        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockResults = (AggregationResults<Document>) org.mockito.Mockito.mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(expectedResults);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class)))
                .thenReturn(mockResults);

        // When
        List<Document> results = queryService.executeMongoQuery(mongoQuery);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("rev-1", results.get(0).get("_id"));
    }

    @Test
    void executeMongoQueryWithList_ShouldHandleEmptyResults() {
        // Given
        List<Map<String, Object>> mongoQuery = List.of(
                Map.of("$match", Map.of("systemCode", "NONEXISTENT"))
        );

        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockResults = (AggregationResults<Document>) org.mockito.Mockito.mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(List.of());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class)))
                .thenReturn(mockResults);

        // When
        List<Document> results = queryService.executeMongoQuery(mongoQuery);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void executeMongoQueryWithList_ShouldHandleProjectStage() {
        // Given - Test the special case for $project handling
        List<Map<String, Object>> mongoQuery = List.of(
                Map.of("$match", Map.of("documentState", "ACTIVE")),
                // Empty project should be replaced with {_id: 1}
                Map.of("$project", Map.of())
        );

        Document doc1 = new Document("_id", "rev-1");
        List<Document> expectedResults = List.of(doc1);

        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockResults = (AggregationResults<Document>) org.mockito.Mockito.mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(expectedResults);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class)))
                .thenReturn(mockResults);

        // When
        List<Document> results = queryService.executeMongoQuery(mongoQuery);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void executeMongoQueryWithList_ShouldHandleMultipleStages() {
        // Given
        List<Map<String, Object>> mongoQuery = List.of(
                Map.of("$match", Map.of("documentState", "ACTIVE")),
                Map.of("$group", Map.of("_id", "$systemCode", "count", Map.of("$sum", 1))),
                Map.of("$sort", Map.of("count", -1)),
                Map.of("$limit", 10)
        );

        Document doc1 = new Document("_id", "SYS-123").append("count", 5);
        Document doc2 = new Document("_id", "SYS-456").append("count", 3);
        List<Document> expectedResults = List.of(doc1, doc2);

        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockResults = (AggregationResults<Document>) org.mockito.Mockito.mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(expectedResults);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class)))
                .thenReturn(mockResults);

        // When
        List<Document> results = queryService.executeMongoQuery(mongoQuery);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("SYS-123", results.get(0).get("_id"));
        assertEquals(5, results.get(0).get("count"));
    }

    @Test
    void executeMongoQueryWithList_ShouldHandleMongoTemplateException() {
        // Given
        List<Map<String, Object>> mongoQuery = List.of(
                Map.of("$match", Map.of("invalid", "query"))
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class)))
                .thenThrow(new RuntimeException("MongoDB connection error"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.executeMongoQuery(mongoQuery));

        assertTrue(exception.getMessage().contains("Failed to execute aggregation pipeline"));
    }

    @Test
    void executeMongoQueryWithList_ShouldUseSolutionReviewsCollection() {
        // Given
        List<Map<String, Object>> mongoQuery = List.of(
                Map.of("$match", Map.of("test", "value"))
        );

        @SuppressWarnings("unchecked")
        AggregationResults<Document> mockResults = (AggregationResults<Document>) org.mockito.Mockito.mock(AggregationResults.class);
        when(mockResults.getMappedResults()).thenReturn(List.of());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class)))
                .thenReturn(mockResults);

        // When
        queryService.executeMongoQuery(mongoQuery);

        // Then - Verify that "solutionReviews" collection is used
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("solutionReviews"), eq(Document.class));
    }
}

