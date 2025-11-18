package com.project.core_service.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GenerateQueryRequestDTO}.
 */
class GenerateQueryRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void builder_AllFields_Success() {
        // Arrange
        String userPrompt = "Find all active solution reviews";
        String lookupName = "business-capabilities";
        List<String> lookupFieldsUsed = Arrays.asList("L1", "L2", "L3");

        // Act
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt(userPrompt)
                .lookupName(lookupName)
                .lookupFieldsUsed(lookupFieldsUsed)
                .build();

        // Assert
        assertNotNull(dto);
        assertEquals(userPrompt, dto.getUserPrompt());
        assertEquals(lookupName, dto.getLookupName());
        assertEquals(lookupFieldsUsed, dto.getLookupFieldsUsed());
        assertEquals(3, dto.getLookupFieldsUsed().size());

        // Validate
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_NullUserPrompt_HasViolation() {
        // Arrange
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt(null)
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("userPrompt")));
    }

    @Test
    void validation_EmptyUserPrompt_HasViolation() {
        // Arrange
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt("")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("userPrompt")));
    }

    @Test
    void validation_NullLookupName_HasViolation() {
        // Arrange
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName(null)
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lookupName")));
    }

    @Test
    void validation_EmptyLookupName_HasViolation() {
        // Arrange
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName("")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lookupName")));
    }

    @Test
    void validation_NullLookupFields_HasViolation() {
        // Arrange
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(null)
                .build();

        // Act
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lookupFieldsUsed")));
    }

    @Test
    void validation_EmptyLookupFields_HasViolation() {
        // Arrange
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(List.of())
                .build();

        // Act
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lookupFieldsUsed")));
    }

    @Test
    void noArgsConstructor_Success() {
        // Act
        GenerateQueryRequestDTO dto = new GenerateQueryRequestDTO();

        // Assert
        assertNotNull(dto);
    }

    @Test
    void allArgsConstructor_Success() {
        // Arrange
        String userPrompt = "Find all active solution reviews";
        String lookupName = "business-capabilities";
        List<String> lookupFieldsUsed = Arrays.asList("L1", "L2");

        // Act
        GenerateQueryRequestDTO dto = new GenerateQueryRequestDTO(
                userPrompt, lookupName, lookupFieldsUsed);

        // Assert
        assertNotNull(dto);
        assertEquals(userPrompt, dto.getUserPrompt());
        assertEquals(lookupName, dto.getLookupName());
        assertEquals(lookupFieldsUsed, dto.getLookupFieldsUsed());

        // Validate
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void setters_Success() {
        // Arrange
        GenerateQueryRequestDTO dto = new GenerateQueryRequestDTO();
        String userPrompt = "Find all active solution reviews";
        String lookupName = "business-capabilities";
        List<String> lookupFieldsUsed = Arrays.asList("L1", "L2");

        // Act
        dto.setUserPrompt(userPrompt);
        dto.setLookupName(lookupName);
        dto.setLookupFieldsUsed(lookupFieldsUsed);

        // Assert
        assertEquals(userPrompt, dto.getUserPrompt());
        assertEquals(lookupName, dto.getLookupName());
        assertEquals(lookupFieldsUsed, dto.getLookupFieldsUsed());

        // Validate
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void builder_SingleField_Success() {
        // Arrange
        List<String> singleField = List.of("L1");

        // Act
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName("test-lookup")
                .lookupFieldsUsed(singleField)
                .build();

        // Assert
        assertNotNull(dto);
        assertEquals(1, dto.getLookupFieldsUsed().size());
        assertEquals("L1", dto.getLookupFieldsUsed().get(0));

        // Validate
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void builder_MultipleFields_Success() {
        // Arrange
        List<String> multipleFields = Arrays.asList("field1", "field2", "field3", "field4", "field5");

        // Act
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt("Complex query")
                .lookupName("complex-lookup")
                .lookupFieldsUsed(multipleFields)
                .build();

        // Assert
        assertNotNull(dto);
        assertEquals(5, dto.getLookupFieldsUsed().size());
        assertTrue(dto.getLookupFieldsUsed().contains("field1"));
        assertTrue(dto.getLookupFieldsUsed().contains("field5"));

        // Validate
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void builder_LongPrompt_Success() {
        // Arrange
        String longPrompt = "This is a very long prompt that describes a complex query requirement " +
                "with multiple conditions and aggregations that need to be performed " +
                "on the MongoDB collection for solution reviews.";

        // Act
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt(longPrompt)
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Assert
        assertNotNull(dto);
        assertEquals(longPrompt, dto.getUserPrompt());
        assertTrue(dto.getUserPrompt().length() > 100);

        // Validate
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void builder_SpecialCharactersInFields_Success() {
        // Arrange
        String promptWithSpecialChars = "Find reviews where status != 'DRAFT' AND code LIKE 'SYS-%'";
        String lookupWithDashes = "tech-eol-dates";
        List<String> fieldsWithSpaces = Arrays.asList("Product Name", "Product Version");

        // Act
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt(promptWithSpecialChars)
                .lookupName(lookupWithDashes)
                .lookupFieldsUsed(fieldsWithSpaces)
                .build();

        // Assert
        assertNotNull(dto);
        assertEquals(promptWithSpecialChars, dto.getUserPrompt());
        assertEquals(lookupWithDashes, dto.getLookupName());
        assertTrue(dto.getLookupFieldsUsed().contains("Product Name"));

        // Validate
        Set<ConstraintViolation<GenerateQueryRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        GenerateQueryRequestDTO dto1 = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName("test-lookup")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        GenerateQueryRequestDTO dto2 = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName("test-lookup")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act & Assert
        assertEquals(dto1, dto2);
    }

    @Test
    void hashCode_SameValues_ReturnsSameHashCode() {
        // Arrange
        GenerateQueryRequestDTO dto1 = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName("test-lookup")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        GenerateQueryRequestDTO dto2 = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName("test-lookup")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act & Assert
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void toString_ContainsAllFields() {
        // Arrange
        GenerateQueryRequestDTO dto = GenerateQueryRequestDTO.builder()
                .userPrompt("Test prompt")
                .lookupName("test-lookup")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act
        String result = dto.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("userPrompt"));
        assertTrue(result.contains("lookupName"));
        assertTrue(result.contains("lookupFieldsUsed"));
    }
}
