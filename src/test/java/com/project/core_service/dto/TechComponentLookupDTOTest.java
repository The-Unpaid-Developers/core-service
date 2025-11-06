package com.project.core_service.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TechComponentLookupDTO Tests")
class TechComponentLookupDTOTest {

    private TechComponentLookupDTO dto;

    @BeforeEach
    void setUp() {
        dto = new TechComponentLookupDTO("Spring Boot", "3.2");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create DTO with no-args constructor")
        void shouldCreateDTOWithNoArgsConstructor() {
            // Act
            TechComponentLookupDTO emptyDto = new TechComponentLookupDTO();

            // Assert
            assertNotNull(emptyDto);
            assertNull(emptyDto.getProductName());
            assertNull(emptyDto.getProductVersion());
        }

        @Test
        @DisplayName("Should create DTO with all-args constructor")
        void shouldCreateDTOWithAllArgsConstructor() {
            // Act
            TechComponentLookupDTO newDto = new TechComponentLookupDTO("Node.js", "20.x");

            // Assert
            assertNotNull(newDto);
            assertEquals("Node.js", newDto.getProductName());
            assertEquals("20.x", newDto.getProductVersion());
        }

        @Test
        @DisplayName("Should create DTO with null values")
        void shouldCreateDTOWithNullValues() {
            // Act
            TechComponentLookupDTO nullDto = new TechComponentLookupDTO(null, null);

            // Assert
            assertNotNull(nullDto);
            assertNull(nullDto.getProductName());
            assertNull(nullDto.getProductVersion());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set productName")
        void shouldGetAndSetProductName() {
            // Arrange
            TechComponentLookupDTO testDto = new TechComponentLookupDTO();
            String expectedProductName = ".NET Core";

            // Act
            testDto.setProductName(expectedProductName);

            // Assert
            assertEquals(expectedProductName, testDto.getProductName());
        }

        @Test
        @DisplayName("Should get and set productVersion")
        void shouldGetAndSetProductVersion() {
            // Arrange
            TechComponentLookupDTO testDto = new TechComponentLookupDTO();
            String expectedVersion = "8.0";

            // Act
            testDto.setProductVersion(expectedVersion);

            // Assert
            assertEquals(expectedVersion, testDto.getProductVersion());
        }

        @Test
        @DisplayName("Should handle null values in setters")
        void shouldHandleNullValuesInSetters() {
            // Arrange
            TechComponentLookupDTO testDto = new TechComponentLookupDTO("test1", "test2");

            // Act
            testDto.setProductName(null);
            testDto.setProductVersion(null);

            // Assert
            assertNull(testDto.getProductName());
            assertNull(testDto.getProductVersion());
        }

        @Test
        @DisplayName("Should handle empty strings in setters")
        void shouldHandleEmptyStringsInSetters() {
            // Arrange
            TechComponentLookupDTO testDto = new TechComponentLookupDTO();

            // Act
            testDto.setProductName("");
            testDto.setProductVersion("");

            // Assert
            assertEquals("", testDto.getProductName());
            assertEquals("", testDto.getProductVersion());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void shouldBeEqualWhenAllFieldsAreTheSame() {
            // Arrange
            TechComponentLookupDTO dto1 = new TechComponentLookupDTO("Java", "17");
            TechComponentLookupDTO dto2 = new TechComponentLookupDTO("Java", "17");

            // Act & Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when productName differs")
        void shouldNotBeEqualWhenProductNameDiffers() {
            // Arrange
            TechComponentLookupDTO dto1 = new TechComponentLookupDTO("Java", "17");
            TechComponentLookupDTO dto2 = new TechComponentLookupDTO("Python", "17");

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal when productVersion differs")
        void shouldNotBeEqualWhenProductVersionDiffers() {
            // Arrange
            TechComponentLookupDTO dto1 = new TechComponentLookupDTO("Java", "17");
            TechComponentLookupDTO dto2 = new TechComponentLookupDTO("Java", "21");

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal when compared to null")
        void shouldNotBeEqualWhenComparedToNull() {
            // Act & Assert
            assertNotEquals(null, dto);
        }

        @Test
        @DisplayName("Should not be equal when compared to different type")
        void shouldNotBeEqualWhenComparedToDifferentType() {
            // Act & Assert
            assertNotEquals("not a DTO", dto);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Act & Assert
            assertEquals(dto, dto);
            assertEquals(dto.hashCode(), dto.hashCode());
        }

        @Test
        @DisplayName("Should handle equality with null fields")
        void shouldHandleEqualityWithNullFields() {
            // Arrange
            TechComponentLookupDTO dto1 = new TechComponentLookupDTO(null, null);
            TechComponentLookupDTO dto2 = new TechComponentLookupDTO(null, null);

            // Act & Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate toString with all fields")
        void shouldGenerateToStringWithAllFields() {
            // Act
            String result = dto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("TechComponentLookupDTO"));
            assertTrue(result.contains("productName=Spring Boot"));
            assertTrue(result.contains("productVersion=3.2"));
        }

        @Test
        @DisplayName("Should generate toString with null fields")
        void shouldGenerateToStringWithNullFields() {
            // Arrange
            TechComponentLookupDTO nullDto = new TechComponentLookupDTO(null, null);

            // Act
            String result = nullDto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("TechComponentLookupDTO"));
            assertTrue(result.contains("productName=null"));
            assertTrue(result.contains("productVersion=null"));
        }

        @Test
        @DisplayName("Should generate toString with mixed null and non-null fields")
        void shouldGenerateToStringWithMixedFields() {
            // Arrange
            TechComponentLookupDTO mixedDto = new TechComponentLookupDTO("React", null);

            // Act
            String result = mixedDto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("productName=React"));
            assertTrue(result.contains("productVersion=null"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // Arrange
            TechComponentLookupDTO emptyDto = new TechComponentLookupDTO("", "");

            // Act & Assert
            assertEquals("", emptyDto.getProductName());
            assertEquals("", emptyDto.getProductVersion());
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Arrange
            String longString = "A".repeat(1000);
            TechComponentLookupDTO longDto = new TechComponentLookupDTO(longString, longString);

            // Act & Assert
            assertEquals(longString, longDto.getProductName());
            assertEquals(longString, longDto.getProductVersion());
            assertEquals(1000, longDto.getProductName().length());
        }

        @Test
        @DisplayName("Should handle special characters")
        void shouldHandleSpecialCharacters() {
            // Arrange
            String specialChars = "@#$%^&*()_+{}|:<>?[];',./~`";
            TechComponentLookupDTO specialDto = new TechComponentLookupDTO(specialChars, specialChars);

            // Act & Assert
            assertEquals(specialChars, specialDto.getProductName());
            assertEquals(specialChars, specialDto.getProductVersion());
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Arrange
            String unicode = "技术组件 版本";
            TechComponentLookupDTO unicodeDto = new TechComponentLookupDTO(unicode, unicode);

            // Act & Assert
            assertEquals(unicode, unicodeDto.getProductName());
            assertEquals(unicode, unicodeDto.getProductVersion());
        }

        @Test
        @DisplayName("Should handle version with dots and special formats")
        void shouldHandleVersionFormats() {
            // Arrange
            TechComponentLookupDTO dto1 = new TechComponentLookupDTO("Product 1", "1.2.3");
            TechComponentLookupDTO dto2 = new TechComponentLookupDTO("Product 2", "v2.0.0");
            TechComponentLookupDTO dto3 = new TechComponentLookupDTO("Product 3", "20.x");

            // Act & Assert
            assertEquals("1.2.3", dto1.getProductVersion());
            assertEquals("v2.0.0", dto2.getProductVersion());
            assertEquals("20.x", dto3.getProductVersion());
        }
    }
}
