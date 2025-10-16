package com.project.core_service.dto;

import com.project.core_service.models.lookup.Lookup;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LookupDTOTest {

    @Test
    void builder_AllFields_Success() {
        // Arrange
        List<Lookup> lookups = Arrays.asList(
            createMockLookup("lookup1", "Test Lookup 1"),
            createMockLookup("lookup2", "Test Lookup 2")
        );

        // Act
        LookupDTO dto = LookupDTO.builder()
                .success(true)
                .lookupName("test-lookup")
                .recordsProcessed(100)
                .totalLookups(2)
                .message("Success message")
                .lookups(lookups)
                .build();

        // Assert
        assertTrue(dto.isSuccess());
        assertEquals("test-lookup", dto.getLookupName());
        assertEquals(Integer.valueOf(100), dto.getRecordsProcessed());
        assertEquals(Integer.valueOf(2), dto.getTotalLookups());
        assertEquals("Success message", dto.getMessage());
        assertEquals(2, dto.getLookups().size());
    }

    @Test
    void builder_MinimalFields_Success() {
        // Act
        LookupDTO dto = LookupDTO.builder()
                .success(false)
                .message("Error occurred")
                .build();

        // Assert
        assertFalse(dto.isSuccess());
        assertEquals("Error occurred", dto.getMessage());
        assertNull(dto.getLookupName());
        assertNull(dto.getRecordsProcessed()); // Changed expectation
        assertNull(dto.getTotalLookups()); // Changed expectation
        assertNull(dto.getLookups());
    }

    @Test
    void builder_EmptyLookupsList_Success() {
        // Act
        LookupDTO dto = LookupDTO.builder()
                .lookups(new ArrayList<>())
                .totalLookups(0)
                .build();

        // Assert
        assertNotNull(dto.getLookups());
        assertTrue(dto.getLookups().isEmpty());
        assertEquals(Integer.valueOf(0), dto.getTotalLookups());
    }

    @Test
    void builder_NullLookupsList_Success() {
        // Act
        LookupDTO dto = LookupDTO.builder()
                .lookups(null)
                .build();

        // Assert
        assertNull(dto.getLookups());
    }

    @Test
    void settersAndGetters_AllFields_Success() {
        // Arrange
        LookupDTO dto = new LookupDTO();
        List<Lookup> lookups = Arrays.asList(createMockLookup("test", "Test"));

        // Act
        dto.setSuccess(true);
        dto.setLookupName("setter-test");
        dto.setRecordsProcessed(50);
        dto.setTotalLookups(1);
        dto.setMessage("Setter message");
        dto.setLookups(lookups);

        // Assert
        assertTrue(dto.isSuccess());
        assertEquals("setter-test", dto.getLookupName());
        assertEquals(Integer.valueOf(50), dto.getRecordsProcessed());
        assertEquals(Integer.valueOf(1), dto.getTotalLookups());
        assertEquals("Setter message", dto.getMessage());
        assertEquals(1, dto.getLookups().size());
    }

    @Test
    void defaultConstructor_Success() {
        // Act
        LookupDTO dto = new LookupDTO();

        // Assert
        assertFalse(dto.isSuccess()); // boolean defaults to false
        assertNull(dto.getLookupName());
        assertNull(dto.getRecordsProcessed()); // Changed expectation
        assertNull(dto.getTotalLookups()); // Changed expectation
        assertNull(dto.getMessage());
        assertNull(dto.getLookups());
    }

    @Test
    void toString_ContainsAllFields() {
        // Arrange
        LookupDTO dto = LookupDTO.builder()
                .success(true)
                .lookupName("test")
                .recordsProcessed(10)
                .totalLookups(1)
                .message("Test message")
                .build();

        // Act
        String result = dto.toString();

        // Assert
        assertTrue(result.contains("success=true"));
        assertTrue(result.contains("lookupName=test"));
        assertTrue(result.contains("recordsProcessed=10"));
        assertTrue(result.contains("totalLookups=1"));
        assertTrue(result.contains("message=Test message"));
    }

    @Test
    void equals_SameContent_ReturnsTrue() {
        // Arrange
        LookupDTO dto1 = LookupDTO.builder()
                .success(true)
                .lookupName("test")
                .recordsProcessed(10)
                .build();

        LookupDTO dto2 = LookupDTO.builder()
                .success(true)
                .lookupName("test")
                .recordsProcessed(10)
                .build();

        // Act & Assert
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void equals_DifferentContent_ReturnsFalse() {
        // Arrange
        LookupDTO dto1 = LookupDTO.builder()
                .success(true)
                .lookupName("test1")
                .build();

        LookupDTO dto2 = LookupDTO.builder()
                .success(true)
                .lookupName("test2")
                .build();

        // Act & Assert
        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_NullComparison_ReturnsFalse() {
        // Arrange
        LookupDTO dto = LookupDTO.builder().success(true).build();

        // Act & Assert
        assertNotEquals(null, dto);
    }

    @Test
    void equals_DifferentClass_ReturnsFalse() {
        // Arrange
        LookupDTO dto = LookupDTO.builder().success(true).build();
        String other = "not a dto";

        // Act & Assert
        assertNotEquals(other, dto);
    }

    private Lookup createMockLookup(String id, String name) {
        Map<String, String> data = new HashMap<>();
        data.put("field1", "value1");
        
        return Lookup.builder()
                .id(id)
                .lookupName(name)
                .data(Arrays.asList(data))
                .recordCount(1)
                .uploadedAt(new Date())
                .build();
    }
}