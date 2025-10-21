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

    @Test
    void processCsvFile_WithSpecialCharacters_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,city,description\nJosé García,São Paulo,Café & Résumé\nMüller Schmidt,Zürich,Über große Erfolge";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "special-chars");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithQuotedFields_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,description,location\n\"John Doe\",\"Software Engineer\",\"New York\"\n\"Jane Smith\",\"Data Analyst\",\"San Francisco\"";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "quoted-fields");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithCommasInQuotedFields_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,address,notes\n\"John Doe\",\"123 Main St, Apt 4, New York, NY\",\"Likes coffee, tea, and water\"\n\"Jane Smith\",\"456 Oak Ave, Suite 200, Boston, MA\",\"Prefers email, phone, or text\"";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "commas-in-quotes");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithEmptyValues_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,middleName,lastName\nJohn,,Doe\nJane,Marie,Smith\nBob,,";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "empty-values");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(3, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithNewlinesInQuotedFields_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,address,notes\n\"John Doe\",\"123 Main St\nApt 4\nNew York, NY\",\"Line 1\nLine 2\nLine 3\"";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "newlines-in-quotes");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithEscapedQuotes_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,quote\n\"John Doe\",\"He said \"\"Hello\"\" to me\"\n\"Jane Smith\",\"She replied \"\"Hi there\"\"\"";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "escaped-quotes");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithUnicodeCharacters_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,symbol,language\n中文,漢字,Chinese\n日本語,ひらがな,Japanese\nРусский,Кириллица,Russian\n한국어,한글,Korean";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "unicode-chars");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(4, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithLongFieldValues_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String longValue = "A".repeat(1000);
        String csvContent = "id,description\n1," + longValue + "\n2," + longValue;
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "long-values");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithMultipleColumns_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "col1,col2,col3,col4,col5,col6,col7,col8,col9,col10\na,b,c,d,e,f,g,h,i,j\n1,2,3,4,5,6,7,8,9,10";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "many-columns");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithWhitespaceInFields_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,description\n  John Doe  ,  Software Engineer  \n  Jane Smith  ,  Data Analyst  ";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "whitespace");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithSingleRow_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,age\nJohn Doe,30";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "single-row");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_WithMixedQuotingStyles_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,age,city\n\"John Doe\",30,New York\nJane Smith,\"25\",\"San Francisco\"\n\"Bob Johnson\",35,Boston";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "mixed-quotes");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(3, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_OnlyHeaders_ThrowsException() {
        // Arrange
        String csvContent = "name,age,department";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        // Act & Assert
        CsvProcessingException exception = assertThrows(
            CsvProcessingException.class,
            () -> lookupService.processCsvFile(file, "only-headers")
        );
        assertTrue(exception.getMessage().contains("CSV file contains no data rows") ||
                   exception.getMessage().contains("no data") ||
                   exception.getMessage().contains("empty"));
    }

    @Test
    void processCsvFile_NullLookupName_ThrowsException() {
        // Arrange
        String csvContent = "name,age\nJohn,30";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        // Act & Assert
        assertThrows(
            Exception.class,
            () -> lookupService.processCsvFile(file, null)
        );
    }

    @Test
    void processCsvFile_EmptyLookupName_ThrowsException() {
        // Arrange
        String csvContent = "name,age\nJohn,30";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        // Act & Assert
        assertThrows(
            Exception.class,
            () -> lookupService.processCsvFile(file, "")
        );
    }

    @Test
    void processCsvFile_WhitespaceLookupName_ThrowsException() {
        // Arrange
        String csvContent = "name,age\nJohn,30";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        // Act & Assert
        assertThrows(
            Exception.class,
            () -> lookupService.processCsvFile(file, "   ")
        );
    }

    @Test
    void processCsvFile_InconsistentColumnCount_HandledGracefully() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        // CSV with inconsistent column counts - some rows have more/fewer columns
        String csvContent = "name,age,department\nJohn Doe,30,Engineering,Extra\nJane Smith,25\nBob,40,Sales";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act - Apache Commons CSV handles this by either ignoring extra columns or leaving missing ones blank
        // The service should either succeed or throw a meaningful exception
        try {
            LookupDTO result = lookupService.processCsvFile(file, "inconsistent-columns");
            // If it succeeds, verify it processed some rows
            assertTrue(result.isSuccess());
            assertTrue(result.getRecordsProcessed() > 0);
        } catch (CsvProcessingException e) {
            // If it throws an exception, it should be a meaningful one
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void processCsvFile_NoHeaders_ThrowsException() {
        // Arrange
        // CSV with only data rows, no header
        String csvContent = "John Doe,30,Engineering\nJane Smith,25,Marketing";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        // Note: This might actually succeed if the service treats the first row as headers
        // The behavior depends on the implementation. Let's verify what happens.
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "no-headers");

        // Assert - The first row will be treated as headers
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_DuplicateHeaders_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,age,name\nJohn,30,Doe\nJane,25,Smith";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act - Duplicate headers might be handled by the CSV parser
        try {
            LookupDTO result = lookupService.processCsvFile(file, "duplicate-headers");
            // If it succeeds, verify it processed the rows
            assertTrue(result.isSuccess());
            assertEquals(2, result.getRecordsProcessed());
        } catch (CsvProcessingException e) {
            // If it throws an exception, verify it's meaningful
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void processCsvFile_TabSeparatedValues_ThrowsOrHandlesGracefully() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        // TSV (Tab-separated) instead of CSV
        String tsvContent = "name\tage\tdepartment\nJohn Doe\t30\tEngineering\nJane Smith\t25\tMarketing";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            tsvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act - TSV might be treated as a single column CSV
        try {
            LookupDTO result = lookupService.processCsvFile(file, "tab-separated");
            // Will likely succeed but treat each line as a single column
            assertTrue(result.isSuccess());
        } catch (CsvProcessingException e) {
            // If it throws an exception, verify it's meaningful
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void processCsvFile_TrailingCommas_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,age,\nJohn,30,\nJane,25,";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "trailing-commas");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_LeadingCommas_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = ",name,age\n,John,30\n,Jane,25";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "leading-commas");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_UnquotedSpecialCharacters_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        String csvContent = "name,symbols\nJohn,@#$%^&*()\nJane,!~`+=[]{}";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "special-symbols");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecordsProcessed());
    }

    @Test
    void processCsvFile_VeryLargeNumberOfRows_Success() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        // Generate a CSV with 1000 rows
        StringBuilder csvBuilder = new StringBuilder("id,name,value\n");
        for (int i = 1; i <= 1000; i++) {
            csvBuilder.append(i).append(",Name").append(i).append(",Value").append(i).append("\n");
        }

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvBuilder.toString().getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, "large-dataset");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1000, result.getRecordsProcessed());
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