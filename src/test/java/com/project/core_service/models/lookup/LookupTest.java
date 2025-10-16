package com.project.core_service.models.lookup;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LookupTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void lookup_BuilderPattern_Success() {
        // Arrange & Act
        Date now = new Date();
        List<Map<String, String>> data = Arrays.asList(
            Map.of("name", "John", "age", "30"),
            Map.of("name", "Jane", "age", "25")
        );

        Lookup lookup = Lookup.builder()
            .id("employees")
            .lookupName("Employee Data")
            .data(data)
            .uploadedAt(now)
            .recordCount(2)
            .build();

        // Assert
        assertEquals("employees", lookup.getId());
        assertEquals("Employee Data", lookup.getLookupName());
        assertEquals(2, lookup.getRecordCount());
        assertEquals(now, lookup.getUploadedAt());
        assertEquals(2, lookup.getData().size());
    }

    @Test
    void lookup_JsonSerialization_Success() throws Exception {
        // Arrange
        Lookup lookup = Lookup.builder()
            .id("test")
            .lookupName("Test Lookup")
            .recordCount(1)
            .data(Collections.singletonList(Map.of("key", "value")))
            .uploadedAt(new Date())
            .build();

        // Act - Serialize to JSON
        String json = objectMapper.writeValueAsString(lookup);
        
        // Assert - Deserialize back
        Lookup deserializedLookup = objectMapper.readValue(json, Lookup.class);
        assertEquals(lookup.getId(), deserializedLookup.getId());
        assertEquals(lookup.getLookupName(), deserializedLookup.getLookupName());
        assertEquals(lookup.getRecordCount(), deserializedLookup.getRecordCount());
    }

    @Test
    void lookup_EqualsAndHashCode_Success() {
        // Arrange
        Date date = new Date();
        List<Map<String, String>> data = Arrays.asList(Map.of("key", "value"));

        Lookup lookup1 = Lookup.builder()
            .id("test")
            .lookupName("Test")
            .data(data)
            .uploadedAt(date)
            .recordCount(1)
            .build();

        Lookup lookup2 = Lookup.builder()
            .id("test")
            .lookupName("Test")
            .data(data)
            .uploadedAt(date)
            .recordCount(1)
            .build();

        // Assert
        assertEquals(lookup1, lookup2);
        assertEquals(lookup1.hashCode(), lookup2.hashCode());
    }

    @Test
    void lookup_DefaultConstructor_Success() {
        // Act
        Lookup lookup = new Lookup();

        // Assert
        assertNull(lookup.getId());
        assertNull(lookup.getLookupName());
        assertNull(lookup.getData());
        assertNull(lookup.getUploadedAt());
        assertNull(lookup.getRecordCount());
    }

    @Test
    void lookup_AllArgsConstructor_Success() {
        // Arrange
        Date date = new Date();
        List<Map<String, String>> data = Collections.singletonList(Map.of("key", "value"));

        // Act
        Lookup lookup = new Lookup("test-id", "Test Name", data, date, 1);

        // Assert
        assertEquals("test-id", lookup.getId());
        assertEquals("Test Name", lookup.getLookupName());
        assertEquals(data, lookup.getData());
        assertEquals(date, lookup.getUploadedAt());
        assertEquals(1, lookup.getRecordCount());
    }

    @Test
    void lookup_SettersAndGetters_Success() {
        // Arrange
        Lookup lookup = new Lookup();
        Date date = new Date();
        List<Map<String, String>> data = Collections.singletonList(Map.of("test", "data"));

        // Act
        lookup.setId("setter-test");
        lookup.setLookupName("Setter Test");
        lookup.setData(data);
        lookup.setUploadedAt(date);
        lookup.setRecordCount(5);

        // Assert
        assertEquals("setter-test", lookup.getId());
        assertEquals("Setter Test", lookup.getLookupName());
        assertEquals(data, lookup.getData());
        assertEquals(date, lookup.getUploadedAt());
        assertEquals(5, lookup.getRecordCount());
    }

    @Test
    void lookup_ToString_ContainsAllFields() {
        // Arrange
        Lookup lookup = Lookup.builder()
            .id("test-id")
            .lookupName("Test Lookup")
            .recordCount(10)
            .uploadedAt(new Date())
            .data(Collections.singletonList(Map.of("field", "value")))
            .build();

        // Act
        String result = lookup.toString();

        // Assert
        assertTrue(result.contains("id=test-id"));
        assertTrue(result.contains("lookupName=Test Lookup"));
        assertTrue(result.contains("recordCount=10"));
    }

    @Test
    void lookup_NullFields_HandledCorrectly() {
        // Act
        Lookup lookup = Lookup.builder()
            .id("test")
            .lookupName(null)
            .data(null)
            .uploadedAt(null)
            .recordCount(null)
            .build();

        // Assert
        assertEquals("test", lookup.getId());
        assertNull(lookup.getLookupName());
        assertNull(lookup.getData());
        assertNull(lookup.getUploadedAt());
        assertNull(lookup.getRecordCount());
    }
}