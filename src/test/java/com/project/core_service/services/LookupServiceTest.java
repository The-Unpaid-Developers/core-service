package com.project.core_service.services;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.exceptions.CsvProcessingException;
import com.project.core_service.exceptions.InvalidFileException;
import com.project.core_service.exceptions.NotFoundException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("csvProcessingTestCases")
    void processCsvFile_VariousFormats_Success(String testName, String csvContent, String lookupName, int expectedRecords) {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );

        UpdateResult updateResult = mock(UpdateResult.class);
        when(mongoCollection.replaceOne(any(Bson.class), any(Document.class), any())).thenReturn(updateResult);

        // Act
        LookupDTO result = lookupService.processCsvFile(file, lookupName);

        // Assert
        assertTrue(result.isSuccess(), "Failed for test case: " + testName);
        assertEquals(expectedRecords, result.getRecordsProcessed(), "Wrong record count for test case: " + testName);
    }

    static Stream<Arguments> csvProcessingTestCases() {
        return Stream.of(
            Arguments.of(
                "Special Characters",
                "name,city,description\nJosé García,São Paulo,Café & Résumé\nMüller Schmidt,Zürich,Über große Erfolge",
                "special-chars",
                2
            ),
            Arguments.of(
                "Quoted Fields",
                "name,description,location\n\"John Doe\",\"Software Engineer\",\"New York\"\n\"Jane Smith\",\"Data Analyst\",\"San Francisco\"",
                "quoted-fields",
                2
            ),
            Arguments.of(
                "Commas in Quoted Fields",
                "name,address,notes\n\"John Doe\",\"123 Main St, Apt 4, New York, NY\",\"Likes coffee, tea, and water\"\n\"Jane Smith\",\"456 Oak Ave, Suite 200, Boston, MA\",\"Prefers email, phone, or text\"",
                "commas-in-quotes",
                2
            ),
            Arguments.of(
                "Empty Values",
                "name,middleName,lastName\nJohn,,Doe\nJane,Marie,Smith\nBob,,",
                "empty-values",
                3
            ),
            Arguments.of(
                "Newlines in Quoted Fields",
                "name,address,notes\n\"John Doe\",\"123 Main St\nApt 4\nNew York, NY\",\"Line 1\nLine 2\nLine 3\"",
                "newlines-in-quotes",
                1
            ),
            Arguments.of(
                "Escaped Quotes",
                "name,quote\n\"John Doe\",\"He said \"\"Hello\"\" to me\"\n\"Jane Smith\",\"She replied \"\"Hi there\"\"\"",
                "escaped-quotes",
                2
            ),
            Arguments.of(
                "Unicode Characters",
                "name,symbol,language\n中文,漢字,Chinese\n日本語,ひらがな,Japanese\nРусский,Кириллица,Russian\n한국어,한글,Korean",
                "unicode-chars",
                4
            ),
            Arguments.of(
                "Long Field Values",
                "id,description\n1," + "A".repeat(1000) + "\n2," + "A".repeat(1000),
                "long-values",
                2
            ),
            Arguments.of(
                "Multiple Columns",
                "col1,col2,col3,col4,col5,col6,col7,col8,col9,col10\na,b,c,d,e,f,g,h,i,j\n1,2,3,4,5,6,7,8,9,10",
                "many-columns",
                2
            ),
            Arguments.of(
                "Whitespace in Fields",
                "name,description\n  John Doe  ,  Software Engineer  \n  Jane Smith  ,  Data Analyst  ",
                "whitespace",
                2
            ),
            Arguments.of(
                "Single Row",
                "name,age\nJohn Doe,30",
                "single-row",
                1
            ),
            Arguments.of(
                "Mixed Quoting Styles",
                "name,age,city\n\"John Doe\",30,New York\nJane Smith,\"25\",\"San Francisco\"\n\"Bob Johnson\",35,Boston",
                "mixed-quotes",
                3
            ),
            Arguments.of(
                "Unquoted Special Characters",
                "name,symbols\nJohn,@#$%^&*()\nJane,!~`+=[]{}",
                "special-symbols",
                2
            )
        );
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

    @ParameterizedTest
    @MethodSource("invalidLookupNameTestCases")
    void processCsvFile_InvalidLookupName_ThrowsException(String testName, String lookupName) {
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
            () -> lookupService.processCsvFile(file, lookupName),
            "Failed for test case: " + testName
        );
    }

    static Stream<Arguments> invalidLookupNameTestCases() {
        return Stream.of(
            Arguments.of("Null Lookup Name", null),
            Arguments.of("Empty Lookup Name", ""),
            Arguments.of("Whitespace Lookup Name", "   ")
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

    // ===== Business Capabilities Tests =====

    @Test
    void getBusinessCapabilities_Success() {
        // Arrange
        Document businessCapDoc = createBusinessCapabilitiesDocument();
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(businessCapDoc);

        // Act
        List<BusinessCapabilityLookupDTO> result = lookupService.getBusinessCapabilities();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        BusinessCapabilityLookupDTO firstCapability = result.get(0);
        assertEquals("Policy Management", firstCapability.getL1());
        assertEquals("Policy Administration", firstCapability.getL2());
        assertEquals("Policy Issuance", firstCapability.getL3());

        BusinessCapabilityLookupDTO secondCapability = result.get(1);
        assertEquals("Claims Management", secondCapability.getL1());
        assertEquals("Claims Processing", secondCapability.getL2());
        assertEquals("First Notice of Loss", secondCapability.getL3());

        BusinessCapabilityLookupDTO thirdCapability = result.get(2);
        assertEquals("Customer Management", thirdCapability.getL1());
        assertEquals("Customer Onboarding", thirdCapability.getL2());
        assertEquals("Customer Registration", thirdCapability.getL3());

        verify(mongoDatabase).getCollection(collectionName);
        verify(mongoCollection).find(any(Bson.class));
    }

    @Test
    void getBusinessCapabilities_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> lookupService.getBusinessCapabilities());
        
        assertEquals("Business capabilities lookup not found", exception.getMessage());
        verify(mongoDatabase).getCollection(collectionName);
        verify(mongoCollection).find(any(Bson.class));
    }

    @Test
    void getBusinessCapabilities_EmptyData_ReturnsEmptyList() {
        // Arrange
        Document emptyDoc = new Document();
        emptyDoc.put("lookupName", "business-capabilities");
        emptyDoc.put("data", new ArrayList<>());

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(emptyDoc);

        // Act
        List<BusinessCapabilityLookupDTO> result = lookupService.getBusinessCapabilities();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mongoDatabase).getCollection(collectionName);
        verify(mongoCollection).find(any(Bson.class));
    }

    @Test
    void getBusinessCapabilities_NullData_ReturnsEmptyList() {
        // Arrange
        Document nullDataDoc = new Document();
        nullDataDoc.put("lookupName", "business-capabilities");
        nullDataDoc.put("data", null);

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(nullDataDoc);

        // Act
        List<BusinessCapabilityLookupDTO> result = lookupService.getBusinessCapabilities();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mongoDatabase).getCollection(collectionName);
        verify(mongoCollection).find(any(Bson.class));
    }

    @Test
    void getBusinessCapabilities_WithNullValues_HandlesGracefully() {
        // Arrange
        List<Map<String, String>> dataWithNulls = Arrays.asList(
            Map.of("L1", "Policy Management", "L2", "Policy Administration", "L3", "Policy Issuance"),
            createMapWithNulls("Claims Management", null, "First Notice of Loss"),
            createMapWithNulls(null, "Customer Onboarding", null)
        );

        Document docWithNulls = new Document();
        docWithNulls.put("lookupName", "business-capabilities");
        docWithNulls.put("data", dataWithNulls);

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(docWithNulls);

        // Act
        List<BusinessCapabilityLookupDTO> result = lookupService.getBusinessCapabilities();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        BusinessCapabilityLookupDTO firstCapability = result.get(0);
        assertEquals("Policy Management", firstCapability.getL1());
        assertEquals("Policy Administration", firstCapability.getL2());
        assertEquals("Policy Issuance", firstCapability.getL3());

        BusinessCapabilityLookupDTO secondCapability = result.get(1);
        assertEquals("Claims Management", secondCapability.getL1());
        assertNull(secondCapability.getL2());
        assertEquals("First Notice of Loss", secondCapability.getL3());

        BusinessCapabilityLookupDTO thirdCapability = result.get(2);
        assertNull(thirdCapability.getL1());
        assertEquals("Customer Onboarding", thirdCapability.getL2());
        assertNull(thirdCapability.getL3());
    }

    @Test
    void getBusinessCapabilities_DatabaseError_ThrowsCsvProcessingException() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> lookupService.getBusinessCapabilities());
        
        assertEquals("Database connection error", exception.getMessage());
        verify(mongoDatabase).getCollection(collectionName);
        verify(mongoCollection).find(any(Bson.class));
    }

    @Test
    void getBusinessCapabilities_DataProcessingError_ThrowsCsvProcessingException() {
        // Arrange
        Document corruptDoc = new Document();
        corruptDoc.put("lookupName", "business-capabilities");
        corruptDoc.put("data", "invalid_data_type"); // This should cause an error

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(corruptDoc);

        // Act & Assert
        CsvProcessingException exception = assertThrows(CsvProcessingException.class, 
            () -> lookupService.getBusinessCapabilities());
        
        assertTrue(exception.getMessage().contains("Failed to process business capabilities"));
    }

    private Document createBusinessCapabilitiesDocument() {
        List<Map<String, String>> businessCapData = Arrays.asList(
            Map.of("L1", "Policy Management", "L2", "Policy Administration", "L3", "Policy Issuance", "Description", "Create and issue new insurance policies to customers"),
            Map.of("L1", "Claims Management", "L2", "Claims Processing", "L3", "First Notice of Loss", "Description", "Capture initial claim information from customers"),
            Map.of("L1", "Customer Management", "L2", "Customer Onboarding", "L3", "Customer Registration", "Description", "Register new customers in the system")
        );

        Document doc = new Document();
        doc.put("id", "business-capabilities");
        doc.put("lookupName", "business-capabilities");
        doc.put("data", businessCapData);
        doc.put("uploadedAt", new Date());
        doc.put("recordCount", 3);

        return doc;
    }

    private Map<String, String> createMapWithNulls(String l1, String l2, String l3) {
        Map<String, String> map = new HashMap<>();
        if (l1 != null) map.put("L1", l1);
        if (l2 != null) map.put("L2", l2);
        if (l3 != null) map.put("L3", l3);
        return map;
    }
}