package com.project.core_service.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.project.core_service.dto.GenerateQueryRequestDTO;
import com.project.core_service.exceptions.NotFoundException;
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

    private AiController aiController;

    private GenerateQueryRequestDTO validRequest;
    private static final Long TEST_TIMEOUT_MS = 60000L;

    @BeforeEach
    void setUp() {
        // Manually construct controller with mocks
        aiController = new AiController(openAiQueryGenerationService, TEST_TIMEOUT_MS);

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

    // ===== Error Handling Tests =====

    @Test
    @SuppressWarnings("unchecked")
    void generateQuery_ServiceThrowsException_HandlesErrorGracefully() throws Exception {
        // Arrange
        lenient().doThrow(new NotFoundException("Lookup not found"))
                .when(openAiQueryGenerationService).generateQueryStream(
                        any(String.class),
                        any(String.class),
                        any(List.class),
                        any(SseEmitter.class));

        // Act
        SseEmitter result = aiController.generateQuery(validRequest);

        // Assert
        assertNotNull(result);
        // The error will be handled asynchronously, so we just verify the emitter is
        // created
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateQuery_ServiceThrowsRuntimeException_HandlesErrorGracefully() throws Exception {
        // Arrange
        lenient().doThrow(new RuntimeException("OpenAI API Error"))
                .when(openAiQueryGenerationService).generateQueryStream(
                        any(String.class),
                        any(String.class),
                        any(List.class),
                        any(SseEmitter.class));

        // Act
        SseEmitter result = aiController.generateQuery(validRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateQuery_ServiceThrowsIllegalArgumentException_HandlesErrorGracefully() throws Exception {
        // Arrange
        lenient().doThrow(new IllegalArgumentException("Invalid field"))
                .when(openAiQueryGenerationService).generateQueryStream(
                        any(String.class),
                        any(String.class),
                        any(List.class),
                        any(SseEmitter.class));

        // Act
        SseEmitter result = aiController.generateQuery(validRequest);

        // Assert
        assertNotNull(result);
    }

    // ===== Timeout Configuration Tests =====

    @Test
    void generateQuery_UsesConfiguredTimeout() {
        // Act
        SseEmitter result = aiController.generateQuery(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_TIMEOUT_MS, ReflectionTestUtils.getField(result, "timeout"));
    }

    @Test
    void generateQuery_DifferentTimeout_UsesCorrectValue() {
        // Arrange
        Long customTimeout = 30000L;
        AiController customController = new AiController(openAiQueryGenerationService, customTimeout);

        // Act
        SseEmitter result = customController.generateQuery(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(customTimeout, ReflectionTestUtils.getField(result, "timeout"));
    }

    // ===== Async Execution Tests =====

    @Test
    @SuppressWarnings("unchecked")
    void generateQuery_ExecutesAsynchronously_VerifyServiceCalled() throws Exception {
        // Arrange
        doAnswer(invocation -> {
            // Simulate async execution
            Thread.sleep(50);
            return null;
        }).when(openAiQueryGenerationService).generateQueryStream(
                any(String.class),
                any(String.class),
                any(List.class),
                any(SseEmitter.class));

        // Act
        SseEmitter result = aiController.generateQuery(validRequest);

        // Assert
        assertNotNull(result);
        // Give async task time to execute
        Thread.sleep(100);
        verify(openAiQueryGenerationService).generateQueryStream(
                eq(validRequest.getUserPrompt()),
                eq(validRequest.getLookupName()),
                eq(validRequest.getLookupFieldsUsed()),
                any(SseEmitter.class));
    }

    @Test
    void generateQuery_MultipleRequests_HandlesIndependently() throws Exception {
        // Arrange
        GenerateQueryRequestDTO request1 = GenerateQueryRequestDTO.builder()
                .userPrompt("Query 1")
                .lookupName("lookup1")
                .lookupFieldsUsed(List.of("field1"))
                .build();

        GenerateQueryRequestDTO request2 = GenerateQueryRequestDTO.builder()
                .userPrompt("Query 2")
                .lookupName("lookup2")
                .lookupFieldsUsed(List.of("field2"))
                .build();

        // Act
        SseEmitter result1 = aiController.generateQuery(request1);
        SseEmitter result2 = aiController.generateQuery(request2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        // Verify they are different emitter instances
        assert result1 != result2;
    }

    // ===== Edge Case Tests =====

    @Test
    void generateQuery_EmptyFieldsList_Success() {
        // Arrange
        GenerateQueryRequestDTO emptyFieldsRequest = GenerateQueryRequestDTO.builder()
                .userPrompt("Find all reviews")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(List.of())
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(emptyFieldsRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void generateQuery_VeryShortPrompt_Success() {
        // Arrange
        GenerateQueryRequestDTO shortPromptRequest = GenerateQueryRequestDTO.builder()
                .userPrompt("Show all")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1"))
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(shortPromptRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void generateQuery_MaxLengthPrompt_Success() {
        // Arrange - Create a very long prompt (simulating max length)
        StringBuilder longPrompt = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longPrompt.append("Find all solution reviews with complex criteria. ");
        }

        GenerateQueryRequestDTO maxLengthRequest = GenerateQueryRequestDTO.builder()
                .userPrompt(longPrompt.toString())
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(maxLengthRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void generateQuery_UnicodeCharacters_Success() {
        // Arrange
        GenerateQueryRequestDTO unicodeRequest = GenerateQueryRequestDTO.builder()
                .userPrompt("Find reviews with 中文字符 and émojis 🚀 and symbols €£¥")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(Arrays.asList("L1", "L2"))
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(unicodeRequest);

        // Assert
        assertNotNull(result);
    }

    @Test
    void generateQuery_LotsOfFields_Success() {
        // Arrange
        List<String> manyFields = Arrays.asList(
                "field1", "field2", "field3", "field4", "field5",
                "field6", "field7", "field8", "field9", "field10");

        GenerateQueryRequestDTO manyFieldsRequest = GenerateQueryRequestDTO.builder()
                .userPrompt("Complex query with many fields")
                .lookupName("business-capabilities")
                .lookupFieldsUsed(manyFields)
                .build();

        // Act
        SseEmitter result = aiController.generateQuery(manyFieldsRequest);

        // Assert
        assertNotNull(result);
    }
}
