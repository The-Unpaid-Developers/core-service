package com.project.core_service.dto;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LookupDTOTest {

    @Test
    void builder_AllFields_Success() {
        // Arrange
        List<Map<String, String>> data = Arrays.asList(
            Map.of("name", "John", "age", "30"),
            Map.of("name", "Jane", "age", "25")
        );
        Map<String, String> fieldDescriptions = Map.of(
            "name", "Employee name",
            "age", "Employee age"
        );
        Date now = new Date();

        // Act
        LookupDTO dto = LookupDTO.builder()
                .id("test-lookup")
                .lookupName("test-lookup")
                .data(data)
                .uploadedAt(now)
                .recordCount(2)
                .description("Test description")
                .fieldDescriptions(fieldDescriptions)
                .build();

        // Assert
        assertEquals("test-lookup", dto.getId());
        assertEquals("test-lookup", dto.getLookupName());
        assertEquals(2, dto.getData().size());
        assertEquals(now, dto.getUploadedAt());
        assertEquals(Integer.valueOf(2), dto.getRecordCount());
        assertEquals("Test description", dto.getDescription());
        assertEquals(fieldDescriptions, dto.getFieldDescriptions());
    }

    @Test
    void builder_MinimalFields_Success() {
        // Act
        LookupDTO dto = LookupDTO.builder()
                .id("minimal")
                .lookupName("minimal")
                .build();

        // Assert
        assertEquals("minimal", dto.getId());
        assertEquals("minimal", dto.getLookupName());
        assertNull(dto.getData());
        assertNull(dto.getUploadedAt());
        assertNull(dto.getRecordCount());
        assertNull(dto.getDescription());
        assertNull(dto.getFieldDescriptions());
    }

    @Test
    void builder_EmptyData_Success() {
        // Act
        LookupDTO dto = LookupDTO.builder()
                .id("empty")
                .lookupName("empty")
                .data(new ArrayList<>())
                .recordCount(0)
                .fieldDescriptions(new HashMap<>())
                .build();

        // Assert
        assertNotNull(dto.getData());
        assertTrue(dto.getData().isEmpty());
        assertEquals(Integer.valueOf(0), dto.getRecordCount());
        assertNotNull(dto.getFieldDescriptions());
        assertTrue(dto.getFieldDescriptions().isEmpty());
    }

    @Test
    void builder_NullData_Success() {
        // Act
        LookupDTO dto = LookupDTO.builder()
                .id("null-data")
                .data(null)
                .fieldDescriptions(null)
                .build();

        // Assert
        assertNull(dto.getData());
        assertNull(dto.getFieldDescriptions());
    }

    @Test
    void settersAndGetters_AllFields_Success() {
        // Arrange
        LookupDTO dto = new LookupDTO();
        List<Map<String, String>> data = Arrays.asList(Map.of("key", "value"));
        Map<String, String> fieldDescs = Map.of("key", "Key field");
        Date date = new Date();

        // Act
        dto.setId("setter-test");
        dto.setLookupName("Setter Test");
        dto.setData(data);
        dto.setUploadedAt(date);
        dto.setRecordCount(1);
        dto.setDescription("Setter description");
        dto.setFieldDescriptions(fieldDescs);

        // Assert
        assertEquals("setter-test", dto.getId());
        assertEquals("Setter Test", dto.getLookupName());
        assertEquals(1, dto.getData().size());
        assertEquals(date, dto.getUploadedAt());
        assertEquals(Integer.valueOf(1), dto.getRecordCount());
        assertEquals("Setter description", dto.getDescription());
        assertEquals(fieldDescs, dto.getFieldDescriptions());
    }

    @Test
    void defaultConstructor_Success() {
        // Act
        LookupDTO dto = new LookupDTO();

        // Assert
        assertNull(dto.getId());
        assertNull(dto.getLookupName());
        assertNull(dto.getData());
        assertNull(dto.getUploadedAt());
        assertNull(dto.getRecordCount());
        assertNull(dto.getDescription());
        assertNull(dto.getFieldDescriptions());
    }

    @Test
    void toString_ContainsAllFields() {
        // Arrange
        Map<String, String> fieldDescs = Map.of("field", "Field description");
        LookupDTO dto = LookupDTO.builder()
                .id("test")
                .lookupName("test")
                .recordCount(10)
                .description("Test description")
                .fieldDescriptions(fieldDescs)
                .build();

        // Act
        String result = dto.toString();

        // Assert
        assertTrue(result.contains("id=test"));
        assertTrue(result.contains("lookupName=test"));
        assertTrue(result.contains("recordCount=10"));
        assertTrue(result.contains("description=Test description"));
    }

    @Test
    void equals_SameContent_ReturnsTrue() {
        // Arrange
        Date date = new Date();
        List<Map<String, String>> data = Arrays.asList(Map.of("key", "value"));
        Map<String, String> fieldDescs = Map.of("key", "Key description");

        LookupDTO dto1 = LookupDTO.builder()
                .id("test")
                .lookupName("test")
                .data(data)
                .uploadedAt(date)
                .recordCount(1)
                .description("Test")
                .fieldDescriptions(fieldDescs)
                .build();

        LookupDTO dto2 = LookupDTO.builder()
                .id("test")
                .lookupName("test")
                .data(data)
                .uploadedAt(date)
                .recordCount(1)
                .description("Test")
                .fieldDescriptions(fieldDescs)
                .build();

        // Act & Assert
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void equals_DifferentContent_ReturnsFalse() {
        // Arrange
        LookupDTO dto1 = LookupDTO.builder()
                .id("test1")
                .lookupName("test1")
                .build();

        LookupDTO dto2 = LookupDTO.builder()
                .id("test2")
                .lookupName("test2")
                .build();

        // Act & Assert
        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_NullComparison_ReturnsFalse() {
        // Arrange
        LookupDTO dto = LookupDTO.builder().id("test").build();

        // Act & Assert
        assertNotEquals(null, dto);
    }

    @Test
    void equals_DifferentClass_ReturnsFalse() {
        // Arrange
        LookupDTO dto = LookupDTO.builder().id("test").build();
        String other = "not a dto";

        // Act & Assert
        assertNotEquals(other, dto);
    }
}