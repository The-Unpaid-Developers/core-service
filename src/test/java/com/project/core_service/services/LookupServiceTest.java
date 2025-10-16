package com.project.core_service.services;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.exceptions.CsvProcessingException;
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

import java.io.IOException;
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
        // Remove the unnecessary stubbing - only add when needed
    }

    @Test
    void processCsvFile_ValidCsv_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        
        String csvContent = "name,age,department\nJohn Doe,30,Engineering\nJane Smith,25,Marketing";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.csv", 
            "text/csv", 
            csvContent.getBytes()
        );
        String lookupName = "employees";

        // Mock the replaceOne operation
        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, lookupName);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(lookupName, result.getLookupName());
        assertEquals(2, result.getRecordsProcessed());
        assertEquals("CSV file processed and stored successfully", result.getMessage());
        verify(mongoCollection).replaceOne(any(Bson.class), any(Document.class), any());
    }

    @Test
    void processCsvFile_EmptyFile_ThrowsException() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);

        // Act & Assert
        InvalidFileException exception = assertThrows(
            InvalidFileException.class,
            () -> lookupService.processCsvFile(emptyFile, "test")
        );
        assertEquals("file parameter is required and cannot be empty", exception.getMessage());
    }

    @Test
    void processCsvFile_InvalidFileType_ThrowsException() {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "some content".getBytes()
        );

        // Act & Assert
        InvalidFileException exception = assertThrows(
            InvalidFileException.class,
            () -> lookupService.processCsvFile(txtFile, "test")
        );
        assertEquals("File must be a CSV file", exception.getMessage());
    }

    @Test
    void processCsvFile_CsvWithBOM_Success() throws Exception {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        
        String csvContent = "\uFEFFname,age\nJohn,30";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.csv", 
            "text/csv", 
            csvContent.getBytes("UTF-8")
        );

        // Mock the replaceOne operation
        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "test");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_IOException_ThrowsException() throws Exception {
        // Arrange
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(file.getContentType()).thenReturn("text/csv");
        when(file.getInputStream()).thenThrow(new IOException("IO Error"));

        // Act & Assert
        CsvProcessingException exception = assertThrows(
            CsvProcessingException.class,
            () -> lookupService.processCsvFile(file, "test")
        );
        assertTrue(exception.getMessage().contains("Error parsing CSV file"));
    }

    @Test
    void getAllLookups_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        
        List<Document> documents = Arrays.asList(
            createValidMockDocument("lookup1", "Lookup 1", 10),
            createValidMockDocument("lookup2", "Lookup 2", 20)
        );
        
        // Mock the cursor behavior
        when(mongoCollection.find()).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(mongoCursor);
        
        // Mock cursor iteration
        when(mongoCursor.hasNext()).thenReturn(true, true, false);
        when(mongoCursor.next()).thenReturn(documents.get(0), documents.get(1));

        // Act
        LookupDTO result = lookupService.getAllLookups();

        // Assert
        assertEquals(2, result.getTotalLookups());
        assertNotNull(result.getLookups());
        assertEquals(2, result.getLookups().size());
    }

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
        assertNotNull(result.getLookups());
        assertEquals(1, result.getLookups().size());
        assertEquals(lookupName, result.getLookups().get(0).getId());
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

    @Test
    void deleteLookup_ExistingLookup_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        
        String lookupName = "test-lookup";
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(mongoCollection.deleteOne(any(Bson.class))).thenReturn(deleteResult);

        // Act
        LookupDTO result = lookupService.deleteLookup(lookupName);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(lookupName, result.getLookupName());
        assertEquals("Lookup deleted successfully", result.getMessage());
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

    // Helper method to create valid mock documents
    private Document createValidMockDocument(String id, String name, int recordCount) {
        Document doc = new Document();
        doc.put("_id", id);
        doc.put("lookupName", name);
        doc.put("recordCount", recordCount);
        doc.put("uploadedAt", new Date());
        
        // Create valid data structure that matches what the service expects
        List<Map<String, String>> dataList = Arrays.asList(
            Map.of("field1", "value1", "field2", "value2"),
            Map.of("field1", "value3", "field2", "value4")
        );
        doc.put("data", dataList);
        
        return doc;
    }
}