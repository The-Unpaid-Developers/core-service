package com.project.core_service.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.core_service.dto.CreateQueryRequestDTO;
import com.project.core_service.dto.QueryExecutionRequestDTO;
import com.project.core_service.dto.UpdateQueryRequestDTO;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.query.Query;
import com.project.core_service.repositories.QueryRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
                .mongoQuery("{\"email\": \"test@example.com\"}")
                .build();
    }

    @Test
    void getQueryByName_Found() {
        when(queryRepository.findById("getUserByEmail")).thenReturn(Optional.of(testQuery));

        Optional<Query> result = queryService.getQueryByName("getUserByEmail");

        assertTrue(result.isPresent());
        assertEquals("getUserByEmail", result.get().getName());
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
                .mongoQuery("{\"status\": \"ACTIVE\"}")
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
                .mongoQuery("{\"email\": \"test@example.com\"}")
                .build();

        when(queryRepository.existsById("getUserByEmail")).thenReturn(false);
        when(queryRepository.save(any(Query.class))).thenReturn(testQuery);

        Query result = queryService.createMongoQuery(request);

        assertNotNull(result);
        assertEquals("getUserByEmail", result.getName());
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
                .mongoQuery("{\"test\": \"value\"}")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createMongoQuery(invalidRequest));

        assertEquals("Query name cannot be null or empty", exception.getMessage());
        verify(queryRepository, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("invalidMongoQueryTestCases")
    void createMongoQuery_ThrowsForInvalidQueries(String name, String mongoQuery, String expectedMessageFragment) {
        CreateQueryRequestDTO invalidRequest = CreateQueryRequestDTO.builder()
                .name(name)
                .mongoQuery(mongoQuery)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createMongoQuery(invalidRequest));

        assertTrue(exception.getMessage().contains(expectedMessageFragment));
        verify(queryRepository, never()).save(any());
    }

    static Stream<Arguments> invalidMongoQueryTestCases() {
        return Stream.of(
                Arguments.of("dangerousQuery", "{\"$out\": \"newCollection\"}", "forbidden operation"),
                Arguments.of("invalidJSON", "this is not valid JSON", "valid JSON format"),
                Arguments.of("testQuery", "   ", "cannot be null or empty"));
    }

    @Test
    void createMongoQuery_ThrowsWhenNameAlreadyExists() {
        CreateQueryRequestDTO request = CreateQueryRequestDTO.builder()
                .name("getUserByEmail")
                .mongoQuery("{\"email\": \"test@example.com\"}")
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
                .mongoQuery("{\"email\": \"test@example.com\", \"active\": true}")
                .build();

        Query updatedQuery = Query.builder()
                .name("getUserByEmail")
                .mongoQuery("{\"email\": \"test@example.com\", \"active\": true}")
                .build();

        when(queryRepository.findById("getUserByEmail")).thenReturn(Optional.of(testQuery));
        when(queryRepository.save(any(Query.class))).thenReturn(updatedQuery);

        Query result = queryService.updateMongoQuery("getUserByEmail", request);

        assertNotNull(result);
        assertEquals("{\"email\": \"test@example.com\", \"active\": true}", result.getMongoQuery());
        verify(queryRepository).findById("getUserByEmail");
        verify(queryRepository).save(any(Query.class));
    }

    @Test
    void updateMongoQuery_ThrowsWhenNotFound() {
        UpdateQueryRequestDTO request = UpdateQueryRequestDTO.builder()
                .mongoQuery("{\"test\": \"value\"}")
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
                .mongoQuery("{\"active\": true}")
                .build();

        QueryExecutionRequestDTO request = QueryExecutionRequestDTO.builder()
                .collection("users")
                .limit(10)
                .build();

        Document doc1 = new Document("name", "John").append("active", true);
        Document doc2 = new Document("name", "Jane").append("active", true);
        List<Document> expectedResults = List.of(doc1, doc2);

        when(queryRepository.findById("findActiveUsers")).thenReturn(Optional.of(storedQuery));
        when(mongoTemplate.find(any(), eq(Document.class), eq("users"))).thenReturn(expectedResults);

        // When
        List<Document> results = queryService.executeMongoQuery("findActiveUsers", request);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(queryRepository).findById("findActiveUsers");
        verify(mongoTemplate).find(any(), eq(Document.class), eq("users"));
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
                .mongoQuery("{\"test\": \"value\"}")
                .build();

        QueryExecutionRequestDTO request = QueryExecutionRequestDTO.builder()
                .build();

        when(queryRepository.findById("testQuery")).thenReturn(Optional.of(storedQuery));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.executeMongoQuery("testQuery", request));

        assertTrue(exception.getMessage().contains("Collection name must be specified"));
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
}
