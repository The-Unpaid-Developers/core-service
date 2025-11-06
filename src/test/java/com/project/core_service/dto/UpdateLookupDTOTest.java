package com.project.core_service.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UpdateLookupDTO Tests")
class UpdateLookupDTOTest {

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            "name,age\nJohn,30".getBytes()
        );
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create DTO with no-args constructor")
        void shouldCreateDTOWithNoArgsConstructor() {
            // Act
            UpdateLookupDTO dto = new UpdateLookupDTO();

            // Assert
            assertNotNull(dto);
            assertNull(dto.getDescription());
            assertNull(dto.getLookupFile());
        }

        @Test
        @DisplayName("Should create DTO with all-args constructor")
        void shouldCreateDTOWithAllArgsConstructor() {
            // Act
            UpdateLookupDTO dto = new UpdateLookupDTO("Updated description", mockFile);

            // Assert
            assertNotNull(dto);
            assertEquals("Updated description", dto.getDescription());
            assertEquals(mockFile, dto.getLookupFile());
        }

        @Test
        @DisplayName("Should create DTO with builder")
        void shouldCreateDTOWithBuilder() {
            // Act
            UpdateLookupDTO dto = UpdateLookupDTO.builder()
                .description("Updated description")
                .lookupFile(mockFile)
                .build();

            // Assert
            assertNotNull(dto);
            assertEquals("Updated description", dto.getDescription());
            assertEquals(mockFile, dto.getLookupFile());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set description")
        void shouldGetAndSetDescription() {
            // Arrange
            UpdateLookupDTO dto = new UpdateLookupDTO();

            // Act
            dto.setDescription("New description");

            // Assert
            assertEquals("New description", dto.getDescription());
        }

        @Test
        @DisplayName("Should get and set lookupFile")
        void shouldGetAndSetLookupFile() {
            // Arrange
            UpdateLookupDTO dto = new UpdateLookupDTO();

            // Act
            dto.setLookupFile(mockFile);

            // Assert
            assertEquals(mockFile, dto.getLookupFile());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void shouldBeEqualWhenAllFieldsAreTheSame() {
            // Arrange
            UpdateLookupDTO dto1 = UpdateLookupDTO.builder()
                .description("Description")
                .lookupFile(mockFile)
                .build();

            UpdateLookupDTO dto2 = UpdateLookupDTO.builder()
                .description("Description")
                .lookupFile(mockFile)
                .build();

            // Act & Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when description differs")
        void shouldNotBeEqualWhenDescriptionDiffers() {
            // Arrange
            UpdateLookupDTO dto1 = UpdateLookupDTO.builder()
                .description("Description 1")
                .lookupFile(mockFile)
                .build();

            UpdateLookupDTO dto2 = UpdateLookupDTO.builder()
                .description("Description 2")
                .lookupFile(mockFile)
                .build();

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Arrange
            UpdateLookupDTO dto = UpdateLookupDTO.builder()
                .description("Description")
                .lookupFile(mockFile)
                .build();

            // Act & Assert
            assertEquals(dto, dto);
            assertEquals(dto.hashCode(), dto.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when compared to null")
        void shouldNotBeEqualWhenComparedToNull() {
            // Arrange
            UpdateLookupDTO dto = UpdateLookupDTO.builder()
                .description("Description")
                .lookupFile(mockFile)
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
            UpdateLookupDTO dto = UpdateLookupDTO.builder()
                .description("Test description")
                .lookupFile(mockFile)
                .build();

            // Act
            String result = dto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("description=Test description"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty string description")
        void shouldHandleEmptyStringDescription() {
            // Act
            UpdateLookupDTO dto = UpdateLookupDTO.builder()
                .description("")
                .lookupFile(mockFile)
                .build();

            // Assert
            assertEquals("", dto.getDescription());
        }

        @Test
        @DisplayName("Should handle null file")
        void shouldHandleNullFile() {
            // Act
            UpdateLookupDTO dto = UpdateLookupDTO.builder()
                .description("Description")
                .lookupFile(null)
                .build();

            // Assert
            assertNull(dto.getLookupFile());
        }
    }
}
