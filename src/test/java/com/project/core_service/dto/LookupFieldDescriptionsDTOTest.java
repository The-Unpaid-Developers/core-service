package com.project.core_service.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LookupFieldDescriptionsDTO Tests")
class LookupFieldDescriptionsDTOTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create DTO with no-args constructor")
        void shouldCreateDTOWithNoArgsConstructor() {
            // Act
            LookupFieldDescriptionsDTO dto = new LookupFieldDescriptionsDTO();

            // Assert
            assertNotNull(dto);
            assertNull(dto.getFieldDescriptions());
        }

        @Test
        @DisplayName("Should create DTO with all-args constructor")
        void shouldCreateDTOWithAllArgsConstructor() {
            // Arrange
            Map<String, String> fieldDescriptions = new HashMap<>();
            fieldDescriptions.put("field1", "Description 1");
            fieldDescriptions.put("field2", "Description 2");

            // Act
            LookupFieldDescriptionsDTO dto = new LookupFieldDescriptionsDTO(fieldDescriptions);

            // Assert
            assertNotNull(dto);
            assertEquals(fieldDescriptions, dto.getFieldDescriptions());
            assertEquals(2, dto.getFieldDescriptions().size());
        }

        @Test
        @DisplayName("Should create DTO with builder")
        void shouldCreateDTOWithBuilder() {
            // Arrange
            Map<String, String> fieldDescriptions = new HashMap<>();
            fieldDescriptions.put("name", "Employee name");
            fieldDescriptions.put("age", "Employee age");

            // Act
            LookupFieldDescriptionsDTO dto = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(fieldDescriptions)
                .build();

            // Assert
            assertNotNull(dto);
            assertEquals(fieldDescriptions, dto.getFieldDescriptions());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set fieldDescriptions")
        void shouldGetAndSetFieldDescriptions() {
            // Arrange
            LookupFieldDescriptionsDTO dto = new LookupFieldDescriptionsDTO();
            Map<String, String> fieldDescriptions = new HashMap<>();
            fieldDescriptions.put("field1", "Description 1");

            // Act
            dto.setFieldDescriptions(fieldDescriptions);

            // Assert
            assertEquals(fieldDescriptions, dto.getFieldDescriptions());
            assertEquals(1, dto.getFieldDescriptions().size());
        }

        @Test
        @DisplayName("Should handle null fieldDescriptions")
        void shouldHandleNullFieldDescriptions() {
            // Arrange
            LookupFieldDescriptionsDTO dto = new LookupFieldDescriptionsDTO();

            // Act
            dto.setFieldDescriptions(null);

            // Assert
            assertNull(dto.getFieldDescriptions());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void shouldBeEqualWhenAllFieldsAreTheSame() {
            // Arrange
            Map<String, String> fieldDescriptions = Map.of("field1", "Description 1");

            LookupFieldDescriptionsDTO dto1 = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(fieldDescriptions)
                .build();

            LookupFieldDescriptionsDTO dto2 = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(fieldDescriptions)
                .build();

            // Act & Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when fieldDescriptions differ")
        void shouldNotBeEqualWhenFieldDescriptionsDiffer() {
            // Arrange
            LookupFieldDescriptionsDTO dto1 = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(Map.of("field1", "Description 1"))
                .build();

            LookupFieldDescriptionsDTO dto2 = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(Map.of("field2", "Description 2"))
                .build();

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Arrange
            LookupFieldDescriptionsDTO dto = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(Map.of("field1", "Description 1"))
                .build();

            // Act & Assert
            assertEquals(dto, dto);
            assertEquals(dto.hashCode(), dto.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when compared to null")
        void shouldNotBeEqualWhenComparedToNull() {
            // Arrange
            LookupFieldDescriptionsDTO dto = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(Map.of("field1", "Description 1"))
                .build();

            // Act & Assert
            assertNotEquals(null, dto);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate toString with all fields")
        void shouldGenerateToStringWithAllFields() {
            // Arrange
            Map<String, String> fieldDescriptions = Map.of("field1", "Description 1");

            LookupFieldDescriptionsDTO dto = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(fieldDescriptions)
                .build();

            // Act
            String result = dto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("fieldDescriptions"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty fieldDescriptions map")
        void shouldHandleEmptyFieldDescriptionsMap() {
            // Arrange
            Map<String, String> emptyMap = new HashMap<>();

            // Act
            LookupFieldDescriptionsDTO dto = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(emptyMap)
                .build();

            // Assert
            assertNotNull(dto.getFieldDescriptions());
            assertTrue(dto.getFieldDescriptions().isEmpty());
        }

        @Test
        @DisplayName("Should handle large number of field descriptions")
        void shouldHandleLargeNumberOfFieldDescriptions() {
            // Arrange
            Map<String, String> largeMap = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                largeMap.put("field" + i, "Description " + i);
            }

            // Act
            LookupFieldDescriptionsDTO dto = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(largeMap)
                .build();

            // Assert
            assertEquals(100, dto.getFieldDescriptions().size());
        }

        @Test
        @DisplayName("Should handle special characters in field names and descriptions")
        void shouldHandleSpecialCharacters() {
            // Arrange
            Map<String, String> specialMap = Map.of(
                "field-with-dash", "Description with special chars: @#$%",
                "field_with_underscore", "Description with unicode: 中文"
            );

            // Act
            LookupFieldDescriptionsDTO dto = LookupFieldDescriptionsDTO.builder()
                .fieldDescriptions(specialMap)
                .build();

            // Assert
            assertEquals(2, dto.getFieldDescriptions().size());
            assertTrue(dto.getFieldDescriptions().containsKey("field-with-dash"));
            assertTrue(dto.getFieldDescriptions().containsKey("field_with_underscore"));
        }
    }
}
