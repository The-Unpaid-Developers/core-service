package com.project.core_service.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.core_service.dto.QueryExecutionRequestDTO;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.query.Query;
import com.project.core_service.repositories.QueryRepository;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
                .query("{\"email\": \"test@example.com\"}")
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
                .query("{\"status\": \"ACTIVE\"}")
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
    void createQuery_Success() {
        when(queryRepository.existsById("getUserByEmail")).thenReturn(false);
        when(queryRepository.save(testQuery)).thenReturn(testQuery);

        Query result = queryService.createQuery(testQuery);

        assertNotNull(result);
        assertEquals("getUserByEmail", result.getName());
        verify(queryRepository).existsById("getUserByEmail");
        verify(queryRepository).save(testQuery);
    }

    @Test
    void createQuery_ThrowsWhenNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createQuery(null));

        assertEquals("Query cannot be null", exception.getMessage());
        verify(queryRepository, never()).save(any());
    }

    @Test
    void createQuery_ThrowsWhenNameIsEmpty() {
        Query invalidQuery = Query.builder()
                .name("   ")
                .query("{\"test\": \"value\"}")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createQuery(invalidQuery));

        assertEquals("Query name cannot be null or empty", exception.getMessage());
        verify(queryRepository, never()).save(any());
    }

    @Test
    void createQuery_ThrowsWhenQueryContainsForbiddenOperations() {
        Query invalidQuery = Query.builder()
                .name("dangerousQuery")
                .query("{\"$out\": \"newCollection\"}")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createQuery(invalidQuery));

        assertTrue(exception.getMessage().contains("forbidden operation"));
        verify(queryRepository, never()).save(any());
    }

    @Test
    void createQuery_ThrowsWhenQueryIsNotValidJSON() {
        Query invalidQuery = Query.builder()
                .name("invalidJSON")
                .query("this is not valid JSON")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createQuery(invalidQuery));

        assertTrue(exception.getMessage().contains("valid JSON format"));
        verify(queryRepository, never()).save(any());
    }

    @Test
    void createQuery_ThrowsWhenQueryStringIsEmpty() {
        Query invalidQuery = Query.builder()
                .name("testQuery")
                .query("   ")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createQuery(invalidQuery));

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
        verify(queryRepository, never()).save(any());
    }

    @Test
    void createQuery_ThrowsWhenNameAlreadyExists() {
        when(queryRepository.existsById("getUserByEmail")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.createQuery(testQuery));

        assertEquals("Query with name 'getUserByEmail' already exists", exception.getMessage());
        verify(queryRepository).existsById("getUserByEmail");
        verify(queryRepository, never()).save(any());
    }

    @Test
    void updateQuery_Success() {
        Query updatedQuery = Query.builder()
                .name("getUserByEmail")
                .query("{\"email\": \"test@example.com\", \"active\": true}")
                .build();

        when(queryRepository.findById("getUserByEmail")).thenReturn(Optional.of(testQuery));
        when(queryRepository.save(any(Query.class))).thenReturn(updatedQuery);

        Query result = queryService.updateQuery("getUserByEmail", updatedQuery);

        assertNotNull(result);
        assertEquals("{\"email\": \"test@example.com\", \"active\": true}", result.getQuery());
        verify(queryRepository).findById("getUserByEmail");
        verify(queryRepository).save(any(Query.class));
    }

    @Test
    void updateQuery_ThrowsWhenNotFound() {
        Query updatedQuery = Query.builder()
                .name("nonExistent")
                .query("{\"test\": \"value\"}")
                .build();

        when(queryRepository.findById("nonExistent")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> queryService.updateQuery("nonExistent", updatedQuery));

        assertEquals("Query not found with name: nonExistent", exception.getMessage());
        verify(queryRepository).findById("nonExistent");
        verify(queryRepository, never()).save(any());
    }

    @Test
    void updateQuery_ThrowsWhenUpdatedQueryIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.updateQuery("getUserByEmail", null));

        assertEquals("Updated query cannot be null", exception.getMessage());
        verify(queryRepository, never()).findById(any());
        verify(queryRepository, never()).save(any());
    }

    @Test
    void updateQuery_ThrowsWhenQueryStringIsEmpty() {
        Query invalidQuery = Query.builder()
                .name("getUserByEmail")
                .query("   ")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.updateQuery("getUserByEmail", invalidQuery));

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
        verify(queryRepository, never()).findById(any());
        verify(queryRepository, never()).save(any());
    }

    @Test
    void executeQuery_Success() {
        // Given
        Query storedQuery = Query.builder()
                .name("findActiveUsers")
                .query("{\"active\": true}")
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
        List<Document> results = queryService.executeQuery("findActiveUsers", request);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(queryRepository).findById("findActiveUsers");
        verify(mongoTemplate).find(any(), eq(Document.class), eq("users"));
    }

    @Test
    void executeQuery_ThrowsWhenQueryNotFound() {
        // Given
        QueryExecutionRequestDTO request = QueryExecutionRequestDTO.builder()
                .collection("users")
                .build();

        when(queryRepository.findById("nonExistent")).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> queryService.executeQuery("nonExistent", request));

        assertEquals("Query not found with name: nonExistent", exception.getMessage());
    }

    @Test
    void executeQuery_ThrowsWhenCollectionNotSpecified() {
        // Given
        Query storedQuery = Query.builder()
                .name("testQuery")
                .query("{\"test\": \"value\"}")
                .build();

        QueryExecutionRequestDTO request = QueryExecutionRequestDTO.builder()
                .build();

        when(queryRepository.findById("testQuery")).thenReturn(Optional.of(storedQuery));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queryService.executeQuery("testQuery", request));

        assertTrue(exception.getMessage().contains("Collection name must be specified"));
    }

    @Test
    void deleteQuery_Success() {
        when(queryRepository.existsById("getUserByEmail")).thenReturn(true);
        doNothing().when(queryRepository).deleteById("getUserByEmail");

        assertDoesNotThrow(() -> queryService.deleteQuery("getUserByEmail"));

        verify(queryRepository).existsById("getUserByEmail");
        verify(queryRepository).deleteById("getUserByEmail");
    }

    @Test
    void deleteQuery_ThrowsWhenNotFound() {
        when(queryRepository.existsById("nonExistent")).thenReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> queryService.deleteQuery("nonExistent"));

        assertEquals("Query not found with name: nonExistent", exception.getMessage());
        verify(queryRepository).existsById("nonExistent");
        verify(queryRepository, never()).deleteById(any());
    }
}
