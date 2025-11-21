package com.project.core_service.services;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.dto.LookupFieldDescriptionsDTO;
import com.project.core_service.dto.LookupWODataDTO;
import com.project.core_service.dto.UpdateLookupDTO;
import com.project.core_service.exceptions.InvalidFileException;
import com.project.core_service.exceptions.NotFoundException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LookupServiceTest {

    @Mock
    private MongoDatabase mongoDatabase;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @Mock
    private FindIterable<Document> findIterable;

    @Mock
    private MongoCursor<Document> mongoCursor;

    @InjectMocks
    private LookupService lookupService;

    private final String collectionName = "test_lookups";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(lookupService, "collectionName", collectionName);
    }

    // ===== Create Lookup Tests =====

    @Test
    void createLookup_ValidCsv_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,age,department\nJohn Doe,30,Engineering\nJane Smith,25,Marketing";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        com.project.core_service.dto.CreateLookupDTO createLookupDTO = com.project.core_service.dto.CreateLookupDTO.builder()
            .lookupName("employees")
            .description("Employee lookup")
            .lookupFile(file)
            .build();

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.createLookup(createLookupDTO);

        // Assert
        assertNotNull(result);
        assertEquals("employees", result.getLookupName());
        assertEquals(2, result.getRecordCount());
        assertEquals("Employee lookup", result.getDescription());
        assertNotNull(result.getData());
        assertNotNull(result.getFieldDescriptions());
        verify(mongoCollection).replaceOne(any(Bson.class), any(Document.class), any());
    }

    @Test
    void createLookup_EmptyFile_ThrowsException() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);

        com.project.core_service.dto.CreateLookupDTO createLookupDTO = com.project.core_service.dto.CreateLookupDTO.builder()
            .lookupName("test")
            .description("Test lookup")
            .lookupFile(emptyFile)
            .build();

        // Act & Assert
        InvalidFileException exception = assertThrows(
            InvalidFileException.class,
            () -> lookupService.createLookup(createLookupDTO)
        );
        assertEquals("file parameter is required and cannot be empty", exception.getMessage());
    }

    @Test
    void createLookup_InvalidFileType_ThrowsException() {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "some content".getBytes()
        );

        com.project.core_service.dto.CreateLookupDTO createLookupDTO = com.project.core_service.dto.CreateLookupDTO.builder()
            .lookupName("test")
            .description("Test lookup")
            .lookupFile(txtFile)
            .build();

        // Act & Assert
        InvalidFileException exception = assertThrows(
            InvalidFileException.class,
            () -> lookupService.createLookup(createLookupDTO)
        );
        assertEquals("File must be a CSV file", exception.getMessage());
    }

    // ===== Get All Lookups Tests =====

    @Test
    void getAllLookups_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        List<Document> documents = Arrays.asList(
            createValidMockDocument("lookup1", "Lookup 1", 10),
            createValidMockDocument("lookup2", "Lookup 2", 20)
        );

        when(mongoCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(mongoCursor);
        when(mongoCursor.hasNext()).thenReturn(true, true, false);
        when(mongoCursor.next()).thenReturn(documents.get(0), documents.get(1));

        // Act
        List<LookupWODataDTO> result = lookupService.getAllLookups();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("lookup1", result.getFirst().getId());
        assertEquals("Lookup 1", result.getFirst().getLookupName());
    }

    // ===== Get Lookup By Name Tests =====

    @Test
    void getLookupByName_ExistingLookup_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String lookupName = "test-lookup";
        Document mockDoc = createValidMockDocument(lookupName, "Test Lookup", 5);

        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(mockDoc);

        // Act
        LookupDTO result = lookupService.getLookupByName(lookupName);

        // Assert
        assertNotNull(result);
        assertEquals(lookupName, result.getId());
        assertEquals("Test Lookup", result.getLookupName());
        assertEquals(5, result.getRecordCount());
    }

    @Test
    void getLookupByName_NonExistentLookup_ThrowsException() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> lookupService.getLookupByName("non-existent")
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    // ===== Delete Lookup Tests =====

    @Test
    void deleteLookup_ExistingLookup_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String lookupName = "test-lookup";
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(mongoCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> lookupService.deleteLookup(lookupName));
        verify(mongoCollection).deleteOne(any(Bson.class));
    }

    @Test
    void deleteLookup_NonExistentLookup_ThrowsException() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(0L);
        when(mongoCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> lookupService.deleteLookup("non-existent")
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    // ===== Update Field Descriptions Tests =====

    @Test
    void updateFieldDescriptions_Success() {
        // Arrange
        String lookupName = "test-lookup";
        Map<String, String> fieldDescriptions = Map.of(
            "field1", "Description for field1",
            "field2", "Description for field2"
        );

        LookupFieldDescriptionsDTO contextDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(fieldDescriptions)
            .build();

        Document existingDoc = createValidMockDocument(lookupName, "Test Lookup", 5);

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupFieldDescriptionsDTO result = lookupService.updateFieldDescriptions(lookupName, contextDTO);

        // Assert
        assertNotNull(result);
        assertEquals(fieldDescriptions, result.getFieldDescriptions());
        verify(mongoCollection).replaceOne(any(Bson.class), any(Document.class), any());
    }

    @Test
    void updateFieldDescriptions_LookupNotFound_ThrowsNotFoundException() {
        // Arrange
        String lookupName = "non-existent-lookup";
        LookupFieldDescriptionsDTO contextDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(Map.of("field1", "desc1"))
            .build();

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> lookupService.updateFieldDescriptions(lookupName, contextDTO)
        );

        assertTrue(exception.getMessage().contains("not found"));
        assertTrue(exception.getMessage().contains(lookupName));
    }

    // ===== Get Field Descriptions Tests =====

    @Test
    void getFieldDescriptionsDTO_Success() {
        // Arrange
        String lookupName = "test-lookup";
        Map<String, String> fieldDescriptions = Map.of(
            "field1", "Description 1",
            "field2", "Description 2"
        );

        Document existingDoc = createValidMockDocument(lookupName, "Test Lookup", 5);
        existingDoc.put("fieldDescriptions", fieldDescriptions);

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);

        // Act
        LookupFieldDescriptionsDTO result = lookupService.getFieldDescriptionsDTO(lookupName);

        // Assert
        assertNotNull(result);
        assertEquals(fieldDescriptions, result.getFieldDescriptions());
        assertEquals(2, result.getFieldDescriptions().size());
    }

    @Test
    void getFieldDescriptionsDTO_LookupNotFound_ThrowsNotFoundException() {
        // Arrange
        String lookupName = "non-existent";

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> lookupService.getFieldDescriptionsDTO(lookupName)
        );

        assertTrue(exception.getMessage().contains("not found"));
        assertTrue(exception.getMessage().contains(lookupName));
    }

    // ===== Update Lookup Tests =====

    @Test
    void updateLookup_WithFileAndDescription_Success() {
        // Arrange
        String lookupName = "test-lookup";
        String csvContent = "name,age\nJohn,30\nJane,25";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateLookupDTO updateDTO = UpdateLookupDTO.builder()
            .description("Updated description")
            .lookupFile(file)
            .build();

        Document existingDoc = createValidMockDocument(lookupName, lookupName, 5);
        Map<String, String> existingFieldDescs = Map.of("name", "Old name desc", "id", "ID field");
        existingDoc.put("fieldDescriptions", existingFieldDescs);

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(existingDoc);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.updateLookup(lookupName, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(lookupName, result.getLookupName());
        assertEquals(2, result.getRecordCount());
        assertEquals("Updated description", result.getDescription());
        verify(mongoCollection).replaceOne(any(Bson.class), any(Document.class), any());
    }

    @Test
    void updateLookup_LookupNotFound_ThrowsNotFoundException() {
        // Arrange
        String lookupName = "non-existent";
        UpdateLookupDTO updateDTO = UpdateLookupDTO.builder()
            .description("New description")
            .lookupFile(null)
            .build();

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> lookupService.updateLookup(lookupName, updateDTO)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    // ===== Helper Methods =====

    private Document createValidMockDocument(String id, String name, int recordCount) {
        Document doc = new Document();
        doc.put("_id", id);
        doc.put("lookupName", name);
        doc.put("recordCount", recordCount);
        doc.put("uploadedAt", new Date());
        doc.put("description", "Test description");

        List<Map<String, String>> dataList = Arrays.asList(
            Map.of("field1", "value1", "field2", "value2"),
            Map.of("field1", "value3", "field2", "value4")
        );
        doc.put("data", dataList);

        Map<String, String> fieldDescriptions = Map.of("field1", "Field 1 description", "field2", "Field 2 description");
        doc.put("fieldDescriptions", fieldDescriptions);

        return doc;
    }
}
