package com.project.core_service.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateLookupDTO Tests")
class CreateLookupDTOTest {

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
            CreateLookupDTO dto = new CreateLookupDTO();

            // Assert
            assertNotNull(dto);
            assertNull(dto.getLookupName());
            assertNull(dto.getDescription());
            assertNull(dto.getLookupFile());
        }

        @Test
        @DisplayName("Should create DTO with all-args constructor")
        void shouldCreateDTOWithAllArgsConstructor() {
            // Act
            CreateLookupDTO dto = new CreateLookupDTO("employees", "Employee data", mockFile);

            // Assert
            assertNotNull(dto);
            assertEquals("employees", dto.getLookupName());
            assertEquals("Employee data", dto.getDescription());
            assertEquals(mockFile, dto.getLookupFile());
        }

        @Test
        @DisplayName("Should create DTO with builder")
        void shouldCreateDTOWithBuilder() {
            // Act
            CreateLookupDTO dto = CreateLookupDTO.builder()
                .lookupName("employees")
                .description("Employee data")
                .lookupFile(mockFile)
                .build();

            // Assert
            assertNotNull(dto);
            assertEquals("employees", dto.getLookupName());
            assertEquals("Employee data", dto.getDescription());
            assertEquals(mockFile, dto.getLookupFile());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set lookupName")
        void shouldGetAndSetLookupName() {
            // Arrange
            CreateLookupDTO dto = new CreateLookupDTO();

            // Act
            dto.setLookupName("test-lookup");

            // Assert
            assertEquals("test-lookup", dto.getLookupName());
        }

        @Test
        @DisplayName("Should get and set description")
        void shouldGetAndSetDescription() {
            // Arrange
            CreateLookupDTO dto = new CreateLookupDTO();

            // Act
            dto.setDescription("Test description");

            // Assert
            assertEquals("Test description", dto.getDescription());
        }

        @Test
        @DisplayName("Should get and set lookupFile")
        void shouldGetAndSetLookupFile() {
            // Arrange
            CreateLookupDTO dto = new CreateLookupDTO();

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
            CreateLookupDTO dto1 = CreateLookupDTO.builder()
                .lookupName("employees")
                .description("Employee data")
                .lookupFile(mockFile)
                .build();

            CreateLookupDTO dto2 = CreateLookupDTO.builder()
                .lookupName("employees")
                .description("Employee data")
                .lookupFile(mockFile)
                .build();

            // Act & Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when lookupName differs")
        void shouldNotBeEqualWhenLookupNameDiffers() {
            // Arrange
            CreateLookupDTO dto1 = CreateLookupDTO.builder()
                .lookupName("lookup1")
                .description("Description")
                .lookupFile(mockFile)
                .build();

            CreateLookupDTO dto2 = CreateLookupDTO.builder()
                .lookupName("lookup2")
                .description("Description")
                .lookupFile(mockFile)
                .build();

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Arrange
            CreateLookupDTO dto = CreateLookupDTO.builder()
                .lookupName("employees")
                .description("Employee data")
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
            CreateLookupDTO dto = CreateLookupDTO.builder()
                .lookupName("employees")
                .description("Employee data")
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
            CreateLookupDTO dto = CreateLookupDTO.builder()
                .lookupName("employees")
                .description("Employee data")
                .lookupFile(mockFile)
                .build();

            // Act
            String result = dto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("lookupName=employees"));
            assertTrue(result.contains("description=Employee data"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // Act
            CreateLookupDTO dto = CreateLookupDTO.builder()
                .lookupName("")
                .description("")
                .lookupFile(mockFile)
                .build();

            // Assert
            assertEquals("", dto.getLookupName());
            assertEquals("", dto.getDescription());
        }

        @Test
        @DisplayName("Should handle null file")
        void shouldHandleNullFile() {
            // Act
            CreateLookupDTO dto = CreateLookupDTO.builder()
                .lookupName("test")
                .description("Test")
                .lookupFile(null)
                .build();

            // Assert
            assertNull(dto.getLookupFile());
        }
    }
}
