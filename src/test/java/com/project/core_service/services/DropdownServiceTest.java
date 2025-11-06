package com.project.core_service.services;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
import com.project.core_service.exceptions.NotFoundException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DropdownServiceTest {

    @Mock
    private MongoDatabase mongoDatabase;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @Mock
    private FindIterable<Document> findIterable;

    @InjectMocks
    private DropdownService dropdownService;

    private final String collectionName = "test_lookups";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dropdownService, "collectionName", collectionName);
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
        List<BusinessCapabilityLookupDTO> result = dropdownService.getBusinessCapabilities();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        BusinessCapabilityLookupDTO firstCapability = result.get(0);
        assertEquals("Policy Management", firstCapability.getL1());
        assertEquals("Policy Administration", firstCapability.getL2());
        assertEquals("Policy Issuance", firstCapability.getL3());
    }

    @Test
    void getBusinessCapabilities_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dropdownService.getBusinessCapabilities());

        assertEquals("Business capabilities lookup not found", exception.getMessage());
    }

    @Test
    void getBusinessCapabilities_EmptyData_ReturnsEmptyList() {
        // Arrange
        Document emptyDoc = new Document();
        emptyDoc.put("id", "business-capabilities");
        emptyDoc.put("lookupName", "business-capabilities");
        emptyDoc.put("data", new ArrayList<>());
        emptyDoc.put("uploadedAt", new Date());
        emptyDoc.put("recordCount", 0);

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(emptyDoc);

        // Act
        List<BusinessCapabilityLookupDTO> result = dropdownService.getBusinessCapabilities();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ===== Tech Components Tests =====

    @Test
    void getTechComponents_Success() {
        // Arrange
        Document techComponentsDoc = createTechComponentsDocument();
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(techComponentsDoc);

        // Act
        List<TechComponentLookupDTO> result = dropdownService.getTechComponents();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        TechComponentLookupDTO firstComponent = result.get(0);
        assertEquals("Spring Boot", firstComponent.getProductName());
        assertEquals("3.2", firstComponent.getProductVersion());
    }

    @Test
    void getTechComponents_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> dropdownService.getTechComponents());

        assertEquals("Tech components lookup not found", exception.getMessage());
    }

    @Test
    void getTechComponents_EmptyData_ReturnsEmptyList() {
        // Arrange
        Document emptyDoc = new Document();
        emptyDoc.put("id", "tech_eol");
        emptyDoc.put("lookupName", "tech_eol");
        emptyDoc.put("data", new ArrayList<>());
        emptyDoc.put("uploadedAt", new Date());
        emptyDoc.put("recordCount", 0);

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(emptyDoc);

        // Act
        List<TechComponentLookupDTO> result = dropdownService.getTechComponents();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getTechComponents_WithNullProductName_SkipsEntry() {
        // Arrange
        List<Map<String, String>> techComponentData = Arrays.asList(
            Map.of("Product Name", "Spring Boot", "Product Version", "3.2"),
            Map.of("Product Version", "20.x"), // Missing Product Name
            Map.of("Product Name", ".NET Core", "Product Version", "8")
        );

        Document techDoc = new Document();
        techDoc.put("id", "tech_eol");
        techDoc.put("lookupName", "tech_eol");
        techDoc.put("data", techComponentData);
        techDoc.put("uploadedAt", new Date());
        techDoc.put("recordCount", 3);

        when(mongoDatabase.getCollection(collectionName)).thenReturn(mongoCollection);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(techDoc);

        // Act
        List<TechComponentLookupDTO> result = dropdownService.getTechComponents();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Should skip the entry with null product name
        assertEquals("Spring Boot", result.get(0).getProductName());
        assertEquals(".NET Core", result.get(1).getProductName());
    }

    // ===== Helper Methods =====

    private Document createBusinessCapabilitiesDocument() {
        List<Map<String, String>> businessCapData = Arrays.asList(
            Map.of("L1", "Policy Management", "L2", "Policy Administration", "L3", "Policy Issuance"),
            Map.of("L1", "Claims Management", "L2", "Claims Processing", "L3", "First Notice of Loss"),
            Map.of("L1", "Customer Management", "L2", "Customer Onboarding", "L3", "Customer Registration")
        );

        Document doc = new Document();
        doc.put("id", "business-capabilities");
        doc.put("lookupName", "business-capabilities");
        doc.put("data", businessCapData);
        doc.put("uploadedAt", new Date());
        doc.put("recordCount", 3);
        doc.put("description", "Business capabilities lookup");

        return doc;
    }

    private Document createTechComponentsDocument() {
        List<Map<String, String>> techComponentData = Arrays.asList(
            Map.of("Product Name", "Spring Boot", "Product Version", "3.2"),
            Map.of("Product Name", "Node.js", "Product Version", "20.x"),
            Map.of("Product Name", ".NET Core", "Product Version", "8")
        );

        Document doc = new Document();
        doc.put("id", "tech_eol");
        doc.put("lookupName", "tech_eol");
        doc.put("data", techComponentData);
        doc.put("uploadedAt", new Date());
        doc.put("recordCount", 3);
        doc.put("description", "Tech components lookup");

        return doc;
    }
}
