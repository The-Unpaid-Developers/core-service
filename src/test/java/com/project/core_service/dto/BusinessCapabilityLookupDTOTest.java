package com.project.core_service.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessCapabilityLookupDTO class.
 * Tests constructors, getters/setters, equals/hashCode, and toString methods.
 */
@DisplayName("BusinessCapabilityLookupDTO Tests")
class BusinessCapabilityLookupDTOTest {

    private BusinessCapabilityLookupDTO dto;

    @BeforeEach
    void setUp() {
        dto = new BusinessCapabilityLookupDTO("Policy Management", "Policy Administration", "Policy Issuance");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create DTO with no-args constructor")
        void shouldCreateDTOWithNoArgsConstructor() {
            // Act
            BusinessCapabilityLookupDTO emptyDto = new BusinessCapabilityLookupDTO();

            // Assert
            assertNotNull(emptyDto);
            assertNull(emptyDto.getL1());
            assertNull(emptyDto.getL2());
            assertNull(emptyDto.getL3());
        }

        @Test
        @DisplayName("Should create DTO with all-args constructor")
        void shouldCreateDTOWithAllArgsConstructor() {
            // Act
            BusinessCapabilityLookupDTO newDto = new BusinessCapabilityLookupDTO(
                    "Claims Management",
                    "Claims Processing",
                    "First Notice of Loss");

            // Assert
            assertNotNull(newDto);
            assertEquals("Claims Management", newDto.getL1());
            assertEquals("Claims Processing", newDto.getL2());
            assertEquals("First Notice of Loss", newDto.getL3());
        }

        @Test
        @DisplayName("Should create DTO with null values")
        void shouldCreateDTOWithNullValues() {
            // Act
            BusinessCapabilityLookupDTO nullDto = new BusinessCapabilityLookupDTO(null, null, null);

            // Assert
            assertNotNull(nullDto);
            assertNull(nullDto.getL1());
            assertNull(nullDto.getL2());
            assertNull(nullDto.getL3());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set L1")
        void shouldGetAndSetL1() {
            // Arrange
            BusinessCapabilityLookupDTO testDto = new BusinessCapabilityLookupDTO();
            String expectedL1 = "Customer Management";

            // Act
            testDto.setL1(expectedL1);

            // Assert
            assertEquals(expectedL1, testDto.getL1());
        }

        @Test
        @DisplayName("Should get and set L2")
        void shouldGetAndSetL2() {
            // Arrange
            BusinessCapabilityLookupDTO testDto = new BusinessCapabilityLookupDTO();
            String expectedL2 = "Customer Onboarding";

            // Act
            testDto.setL2(expectedL2);

            // Assert
            assertEquals(expectedL2, testDto.getL2());
        }

        @Test
        @DisplayName("Should get and set L3")
        void shouldGetAndSetL3() {
            // Arrange
            BusinessCapabilityLookupDTO testDto = new BusinessCapabilityLookupDTO();
            String expectedL3 = "Customer Registration";

            // Act
            testDto.setL3(expectedL3);

            // Assert
            assertEquals(expectedL3, testDto.getL3());
        }

        @Test
        @DisplayName("Should handle null values in setters")
        void shouldHandleNullValuesInSetters() {
            // Arrange
            BusinessCapabilityLookupDTO testDto = new BusinessCapabilityLookupDTO("test1", "test2", "test3");

            // Act
            testDto.setL1(null);
            testDto.setL2(null);
            testDto.setL3(null);

            // Assert
            assertNull(testDto.getL1());
            assertNull(testDto.getL2());
            assertNull(testDto.getL3());
        }

        @Test
        @DisplayName("Should handle empty strings in setters")
        void shouldHandleEmptyStringsInSetters() {
            // Arrange
            BusinessCapabilityLookupDTO testDto = new BusinessCapabilityLookupDTO();

            // Act
            testDto.setL1("");
            testDto.setL2("");
            testDto.setL3("");

            // Assert
            assertEquals("", testDto.getL1());
            assertEquals("", testDto.getL2());
            assertEquals("", testDto.getL3());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void shouldBeEqualWhenAllFieldsAreTheSame() {
            // Arrange
            BusinessCapabilityLookupDTO dto1 = new BusinessCapabilityLookupDTO("L1", "L2", "L3");
            BusinessCapabilityLookupDTO dto2 = new BusinessCapabilityLookupDTO("L1", "L2", "L3");

            // Act & Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when L1 differs")
        void shouldNotBeEqualWhenL1Differs() {
            // Arrange
            BusinessCapabilityLookupDTO dto1 = new BusinessCapabilityLookupDTO("L1A", "L2", "L3");
            BusinessCapabilityLookupDTO dto2 = new BusinessCapabilityLookupDTO("L1B", "L2", "L3");

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal when L2 differs")
        void shouldNotBeEqualWhenL2Differs() {
            // Arrange
            BusinessCapabilityLookupDTO dto1 = new BusinessCapabilityLookupDTO("L1", "L2A", "L3");
            BusinessCapabilityLookupDTO dto2 = new BusinessCapabilityLookupDTO("L1", "L2B", "L3");

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal when L3 differs")
        void shouldNotBeEqualWhenL3Differs() {
            // Arrange
            BusinessCapabilityLookupDTO dto1 = new BusinessCapabilityLookupDTO("L1", "L2", "L3A");
            BusinessCapabilityLookupDTO dto2 = new BusinessCapabilityLookupDTO("L1", "L2", "L3B");

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal when compared to null")
        void shouldNotBeEqualWhenComparedToNull() {
            // Act & Assert
            assertNotEquals(dto, null);
        }

        @Test
        @DisplayName("Should not be equal when compared to different type")
        void shouldNotBeEqualWhenComparedToDifferentType() {
            // Act & Assert
            assertNotEquals(dto, "not a DTO");
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
            BusinessCapabilityLookupDTO dto1 = new BusinessCapabilityLookupDTO(null, null, null);
            BusinessCapabilityLookupDTO dto2 = new BusinessCapabilityLookupDTO(null, null, null);

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
            assertTrue(result.contains("BusinessCapabilityLookupDTO"));
            assertTrue(result.contains("l1=Policy Management"));
            assertTrue(result.contains("l2=Policy Administration"));
            assertTrue(result.contains("l3=Policy Issuance"));
        }

        @Test
        @DisplayName("Should generate toString with null fields")
        void shouldGenerateToStringWithNullFields() {
            // Arrange
            BusinessCapabilityLookupDTO nullDto = new BusinessCapabilityLookupDTO(null, null, null);

            // Act
            String result = nullDto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("BusinessCapabilityLookupDTO"));
            assertTrue(result.contains("l1=null"));
            assertTrue(result.contains("l2=null"));
            assertTrue(result.contains("l3=null"));
        }

        @Test
        @DisplayName("Should generate toString with mixed null and non-null fields")
        void shouldGenerateToStringWithMixedFields() {
            // Arrange
            BusinessCapabilityLookupDTO mixedDto = new BusinessCapabilityLookupDTO("Policy Management", null, "Policy Issuance");

            // Act
            String result = mixedDto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("l1=Policy Management"));
            assertTrue(result.contains("l2=null"));
            assertTrue(result.contains("l3=Policy Issuance"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // Arrange
            BusinessCapabilityLookupDTO emptyDto = new BusinessCapabilityLookupDTO("", "", "");

            // Act & Assert
            assertEquals("", emptyDto.getL1());
            assertEquals("", emptyDto.getL2());
            assertEquals("", emptyDto.getL3());
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Arrange
            String longString = "A".repeat(1000);
            BusinessCapabilityLookupDTO longDto = new BusinessCapabilityLookupDTO(longString, longString, longString);

            // Act & Assert
            assertEquals(longString, longDto.getL1());
            assertEquals(longString, longDto.getL2());
            assertEquals(longString, longDto.getL3());
            assertEquals(1000, longDto.getL1().length());
        }

        @Test
        @DisplayName("Should handle special characters")
        void shouldHandleSpecialCharacters() {
            // Arrange
            String specialChars = "L1-@#$%^&*()_+{}|:<>?[];',./~`";
            BusinessCapabilityLookupDTO specialDto = new BusinessCapabilityLookupDTO(specialChars, specialChars, specialChars);

            // Act & Assert
            assertEquals(specialChars, specialDto.getL1());
            assertEquals(specialChars, specialDto.getL2());
            assertEquals(specialChars, specialDto.getL3());
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Arrange
            String unicode = "保险管理 政策管理 政策发行";
            BusinessCapabilityLookupDTO unicodeDto = new BusinessCapabilityLookupDTO(unicode, unicode, unicode);

            // Act & Assert
            assertEquals(unicode, unicodeDto.getL1());
            assertEquals(unicode, unicodeDto.getL2());
            assertEquals(unicode, unicodeDto.getL3());
        }
    }
}