package com.project.core_service.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.project.core_service.dto.GenerateQueryRequestDTO;
import com.project.core_service.services.OpenAiQueryGenerationService;

/**
 * Unit tests for {@link AiController}.
 * 
 * Note: Validation tests for @NotNull and @NotEmpty constraints are handled
 * by Spring's Bean Validation framework. These constraints are validated before
 * the controller method is invoked, returning 400 Bad Request automatically.
 * Those scenarios are best tested via integration tests with MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private OpenAiQueryGenerationService openAiQueryGenerationService;

    @InjectMocks
    private AiController aiController;

    private GenerateQueryRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        validRequest = GenerateQueryRequestDTO.builder()
                .userPrompt("Find all active solution reviews")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();
    }

    // ===== Generate Query Tests =====

    @Test
    void generateQuery_ValidRequest_ReturnsEmitter() {
        // Act
        SseEmitter result = aiController.generateQuery(validRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void generateQuery_ComplexPrompt_Success() {
        // Arrange
        GenerateQueryRequestDTO complexRequest = GenerateQueryRequestDTO.builder()
                .userPrompt("Find all solution reviews where documentState is ACTIVE " +
                        "and businessUnit is PAYMENTS and created in last 30 days, " +
                        "grouped by systemCode with count")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1", "L2", "L3"))
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(complexRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void generateQuery_MultipleFields_Success() {
        // Arrange
        GenerateQueryRequestDTO multiFieldRequest = GenerateQueryRequestDTO.builder()
                .userPrompt("Find all active solution reviews")
                .lookupName("tech-components")
                .lookupFieldsUsed(Arrays.asList("productName", "productVersion", "eolDate"))
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(multiFieldRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void generateQuery_SingleField_Success() {
        // Arrange
        GenerateQueryRequestDTO singleFieldRequest = GenerateQueryRequestDTO.builder()
                .userPrompt("Show all business units")
                .lookupName("business-units")
                .lookupFieldsUsed(List.of("businessUnit"))
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(singleFieldRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void generateQuery_LongPrompt_Success() {
        // Arrange
        String longPrompt = "Find all solution reviews that have the following characteristics: " +
                "documentState must be ACTIVE or APPROVED, " +
                "the systemComponents array must contain at least one component with role BACK_END, " +
                "the solutionOverview.businessUnit must be either PAYMENTS or RISK, " +
                "created within the last 90 days, " +
                "and group the results by solutionOverview.businessDriver " +
                "with a count of documents in each group, " +
                "sorted by count in descending order";

        GenerateQueryRequestDTO longPromptRequest = GenerateQueryRequestDTO.builder()
                .userPrompt(longPrompt)
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(longPromptRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void generateQuery_SpecialCharactersInPrompt_Success() {
        // Arrange
        GenerateQueryRequestDTO specialCharsRequest = GenerateQueryRequestDTO.builder()
                .userPrompt("Find reviews with systemCode matching pattern 'SYS-*' AND status != 'DRAFT'")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(specialCharsRequest);

        // Assert
        assertNotNull(result);
    }
}
