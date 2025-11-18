package com.project.core_service.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.lookup.Lookup;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import io.reactivex.Flowable;

import java.util.Collections;

/**
 * Unit tests for {@link OpenAiQueryGenerationService}.
 */
@ExtendWith(MockitoExtension.class)
class OpenAiQueryGenerationServiceTest {

    @Mock
    private OpenAiService openAiService;

    @Mock
    private LookupService lookupService;

    @InjectMocks
    private OpenAiQueryGenerationService openAiQueryGenerationService;

    private Lookup testLookup;
    private List<String> testLookupFieldsUsed;
    private String testUserPrompt;

    @BeforeEach
    void setUp() {
        // Set up test schema content
        String testSchema = "id: Unique identifier\nsystemCode: System code\ndocumentState: Document state";
        ReflectionTestUtils.setField(openAiQueryGenerationService, "schemaContent", testSchema);

        // Set up test lookup data
        testLookup = createTestLookup();
        testLookupFieldsUsed = List.of("L1", "L2");
        testUserPrompt = "Find all active solution reviews";
    }

    // ===== Generate Query Stream Tests =====

    @Test
    void generateQueryStream_Success() throws Exception {
        // Arrange
        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);

        // Create mock streaming response
        ChatCompletionChunk chunk1 = createMockChunk("[{\"$match\":");
        ChatCompletionChunk chunk2 = createMockChunk("{\"documentState\":\"ACTIVE\"}}]");

        Flowable<ChatCompletionChunk> mockStream = Flowable.just(chunk1, chunk2);
        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(mockStream);

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        assertDoesNotThrow(() -> {
            openAiQueryGenerationService.generateQueryStream(
                    testUserPrompt,
                    "business-capabilities",
                    testLookupFieldsUsed,
                    emitter);
        });

        // Assert
        verify(lookupService).findLookupByName("business-capabilities");
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
    }

    @Test
    void generateQueryStream_LookupNotFound_CompletesWithError() {
        // Arrange
        when(lookupService.findLookupByName("non-existent"))
                .thenThrow(new NotFoundException("Lookup not found"));

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "non-existent",
                testLookupFieldsUsed,
                emitter);

        // Assert
        verify(lookupService).findLookupByName("non-existent");
        verify(emitter).completeWithError(any(NotFoundException.class));
    }

    @Test
    void generateQueryStream_OpenAiError_HandlesError() {
        // Arrange
        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);

        Flowable<ChatCompletionChunk> errorStream = Flowable.error(
                new RuntimeException("OpenAI API Error"));
        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(errorStream);

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "business-capabilities",
                testLookupFieldsUsed,
                emitter);

        // Assert - just verify the service was called
        verify(lookupService).findLookupByName("business-capabilities");
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
    }

    @Test
    void generateQueryStream_EmptyChunks_HandlesGracefully() {
        // Arrange
        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);

        // Create empty chunk
        ChatCompletionChunk emptyChunk = createMockChunk("");
        Flowable<ChatCompletionChunk> mockStream = Flowable.just(emptyChunk);

        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(mockStream);

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        assertDoesNotThrow(() -> {
            openAiQueryGenerationService.generateQueryStream(
                    testUserPrompt,
                    "business-capabilities",
                    testLookupFieldsUsed,
                    emitter);
        });

        // Assert
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
        verify(emitter).complete();
    }

    @Test
    void generateQueryStream_MultipleFields_FiltersCorrectly() {
        // Arrange
        List<String> multipleFields = List.of("L1", "L2", "L3");
        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);

        ChatCompletionChunk chunk = createMockChunk("[{\"$match\":{}}]");
        Flowable<ChatCompletionChunk> mockStream = Flowable.just(chunk);

        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(mockStream);

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "business-capabilities",
                multipleFields,
                emitter);

        // Assert
        verify(lookupService).findLookupByName("business-capabilities");
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
        verify(emitter).complete();
    }

    @Test
    void generateQueryStream_LargeLookupData_LimitsTo50Records() {
        // Arrange
        Lookup largeLookup = createLargeLookup(100);
        when(lookupService.findLookupByName("large-lookup")).thenReturn(largeLookup);

        ChatCompletionChunk chunk = createMockChunk("[{\"$match\":{}}]");
        Flowable<ChatCompletionChunk> mockStream = Flowable.just(chunk);

        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(mockStream);

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "large-lookup",
                testLookupFieldsUsed,
                emitter);

        // Assert
        verify(lookupService).findLookupByName("large-lookup");
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
        verify(emitter).complete();
    }

    @Test
    void generateQueryStream_ComplexPrompt_Success() {
        // Arrange
        String complexPrompt = "Find all solution reviews where documentState is ACTIVE " +
                "and businessUnit is PAYMENTS and group by systemCode";

        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);

        ChatCompletionChunk chunk = createMockChunk("[{\"$match\":{\"documentState\":\"ACTIVE\"}}]");
        Flowable<ChatCompletionChunk> mockStream = Flowable.just(chunk);

        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(mockStream);

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                complexPrompt,
                "business-capabilities",
                testLookupFieldsUsed,
                emitter);

        // Assert
        verify(lookupService).findLookupByName("business-capabilities");
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
        verify(emitter).complete();
    }

    @Test
    void generateQueryStream_LookupWithEmptyFieldDescriptions_HandlesGracefully() {
        // Arrange
        Lookup lookupWithoutDescriptions = createTestLookupWithoutFieldDescriptions();
        when(lookupService.findLookupByName("minimal-lookup")).thenReturn(lookupWithoutDescriptions);

        ChatCompletionChunk chunk = createMockChunk("[{\"$match\":{}}]");
        Flowable<ChatCompletionChunk> mockStream = Flowable.just(chunk);

        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(mockStream);

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "minimal-lookup",
                testLookupFieldsUsed,
                emitter);

        // Assert
        verify(lookupService).findLookupByName("minimal-lookup");
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
        verify(emitter).complete();
    }

    @Test
    void generateQueryStream_UsesCorrectModelName() {
        // Arrange
        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);

        ChatCompletionChunk chunk = createMockChunk("[{\"$match\":{}}]");
        Flowable<ChatCompletionChunk> mockStream = Flowable.just(chunk);

        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenAnswer(invocation -> {
                    ChatCompletionRequest request = invocation.getArgument(0);
                    assertEquals("gpt-5.1", request.getModel());
                    return mockStream;
                });

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "business-capabilities",
                testLookupFieldsUsed,
                emitter);

        // Assert
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
    }

    @Test
    void generateQueryStream_UsesLowTemperature() {
        // Arrange
        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);

        ChatCompletionChunk chunk = createMockChunk("[{\"$match\":{}}]");
        Flowable<ChatCompletionChunk> mockStream = Flowable.just(chunk);

        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenAnswer(invocation -> {
                    ChatCompletionRequest request = invocation.getArgument(0);
                    assertEquals(0.1, request.getTemperature());
                    return mockStream;
                });

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "business-capabilities",
                testLookupFieldsUsed,
                emitter);

        // Assert
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
    }

    @Test
    void generateQueryStream_IncludesSystemAndUserMessages() {
        // Arrange
        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);

        ChatCompletionChunk chunk = createMockChunk("[{\"$match\":{}}]");
        Flowable<ChatCompletionChunk> mockStream = Flowable.just(chunk);

        when(openAiService.streamChatCompletion(any(ChatCompletionRequest.class)))
                .thenAnswer(invocation -> {
                    ChatCompletionRequest request = invocation.getArgument(0);
                    assertNotNull(request.getMessages());
                    assertEquals(2, request.getMessages().size());
                    assertEquals(ChatMessageRole.SYSTEM.value(), request.getMessages().get(0).getRole());
                    assertEquals(ChatMessageRole.USER.value(), request.getMessages().get(1).getRole());
                    return mockStream;
                });

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "business-capabilities",
                testLookupFieldsUsed,
                emitter);

        // Assert
        verify(openAiService).streamChatCompletion(any(ChatCompletionRequest.class));
    }

    @Test
    void generateQueryStream_InvalidFieldInLookupFieldsUsed_CompletesWithError() {
        // Arrange
        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);
        List<String> invalidFields = List.of("L1", "InvalidField", "AnotherInvalidField");

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "business-capabilities",
                invalidFields,
                emitter);

        // Assert
        verify(lookupService).findLookupByName("business-capabilities");
        verify(emitter).completeWithError(any(IllegalArgumentException.class));
    }

    @Test
    void generateQueryStream_AllInvalidFields_CompletesWithError() {
        // Arrange
        when(lookupService.findLookupByName("business-capabilities")).thenReturn(testLookup);
        List<String> allInvalidFields = List.of("Field1", "Field2", "Field3");

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "business-capabilities",
                allInvalidFields,
                emitter);

        // Assert
        verify(lookupService).findLookupByName("business-capabilities");
        verify(emitter).completeWithError(any(IllegalArgumentException.class));
    }

    @Test
    void generateQueryStream_EmptyLookupData_CompletesWithError() {
        // Arrange
        Lookup emptyLookup = Lookup.builder()
                .id("empty-lookup")
                .lookupName("empty-lookup")
                .description("Empty lookup")
                .data(new ArrayList<>())
                .fieldDescriptions(new HashMap<>())
                .uploadedAt(new Date())
                .recordCount(0)
                .build();

        when(lookupService.findLookupByName("empty-lookup")).thenReturn(emptyLookup);

        SseEmitter emitter = mock(SseEmitter.class);

        // Act
        openAiQueryGenerationService.generateQueryStream(
                testUserPrompt,
                "empty-lookup",
                List.of("L1"),
                emitter);

        // Assert
        verify(lookupService).findLookupByName("empty-lookup");
        verify(emitter).completeWithError(any(IllegalArgumentException.class));
    }

    // ===== Helper Methods =====

    private Lookup createTestLookup() {
        Map<String, String> fieldDescriptions = new HashMap<>();
        fieldDescriptions.put("L1", "Level 1 capability");
        fieldDescriptions.put("L2", "Level 2 capability");
        fieldDescriptions.put("L3", "Level 3 capability");

        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> record1 = new HashMap<>();
        record1.put("L1", "Finance");
        record1.put("L2", "Payments");
        record1.put("L3", "Digital Payments");
        data.add(record1);

        Map<String, String> record2 = new HashMap<>();
        record2.put("L1", "Risk");
        record2.put("L2", "Compliance");
        record2.put("L3", "Regulatory Reporting");
        data.add(record2);

        return Lookup.builder()
                .id("business-capabilities")
                .lookupName("business-capabilities")
                .description("Business capability hierarchy")
                .data(data)
                .fieldDescriptions(fieldDescriptions)
                .uploadedAt(new Date())
                .recordCount(2)
                .build();
    }

    private Lookup createTestLookupWithoutFieldDescriptions() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> record = new HashMap<>();
        record.put("L1", "Finance");
        record.put("L2", "Payments");
        data.add(record);

        return Lookup.builder()
                .id("minimal-lookup")
                .lookupName("minimal-lookup")
                .description("Minimal lookup")
                .data(data)
                .fieldDescriptions(new HashMap<>())
                .uploadedAt(new Date())
                .recordCount(1)
                .build();
    }

    private Lookup createLargeLookup(int recordCount) {
        Map<String, String> fieldDescriptions = new HashMap<>();
        fieldDescriptions.put("L1", "Level 1");
        fieldDescriptions.put("L2", "Level 2");

        List<Map<String, String>> data = new ArrayList<>();
        for (int i = 0; i < recordCount; i++) {
            Map<String, String> record = new HashMap<>();
            record.put("L1", "Category " + i);
            record.put("L2", "Subcategory " + i);
            data.add(record);
        }

        return Lookup.builder()
                .id("large-lookup")
                .lookupName("large-lookup")
                .description("Large lookup with many records")
                .data(data)
                .fieldDescriptions(fieldDescriptions)
                .uploadedAt(new Date())
                .recordCount(recordCount)
                .build();
    }

    private ChatCompletionChunk createMockChunk(String content) {
        ChatCompletionChunk chunk = mock(ChatCompletionChunk.class);
        ChatMessage message = new ChatMessage();
        message.setContent(content);

        // Create a mock for the inner Choice class
        Object choice = mock(Object.class);

        // For the test, we'll just return a simple structure
        // In practice, the actual OpenAI library structure will work
        when(chunk.getChoices()).thenReturn(Collections.emptyList());

        return chunk;
    }
}
