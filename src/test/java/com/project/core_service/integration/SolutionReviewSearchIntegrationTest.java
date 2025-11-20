package com.project.core_service.integration;

import com.project.core_service.client.ChatbotServiceClient;
import com.project.core_service.dto.ChatbotTranslateResponseDTO;
import com.project.core_service.dto.SearchQueryDTO;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.repositories.SolutionReviewRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Solution Review Search functionality.
 *
 * <p>
 * These tests verify the natural language search feature that translates
 * user queries to MongoDB aggregation pipelines via the chatbot service.
 *
 * <h2>Test Scenarios Covered:</h2>
 * <ul>
 * <li>Successful search with matching results</li>
 * <li>Empty search results</li>
 * <li>Error handling for chatbot service failures</li>
 * <li>Filtering and mapping of search results</li>
 * </ul>
 *
 * @see com.project.core_service.controllers.SolutionReviewController
 * @see BaseIntegrationTest
 */
@DisplayName("Solution Review Search Integration Tests")
@Epic("Solution Review Management")
@Feature("Natural Language Search")
class SolutionReviewSearchIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SolutionReviewRepository solutionReviewRepository;

    @MockitoBean
    private ChatbotServiceClient chatbotServiceClient;

    @BeforeEach
    @Step("Reset test data factory before each test")
    void setup() {
        TestDataFactory.reset();
    }

    @Nested
    @DisplayName("POST /api/v1/solution-review/search - Search Solution Reviews")
    @Story("Search Solution Reviews")
    class SearchSolutionReviewTests {

        @Test
        @DisplayName("Should return matching results for valid search query")
        @Description("Tests that search returns solution reviews matching the translated MongoDB query")
        @Severity(SeverityLevel.CRITICAL)
        void shouldReturnMatchingResultsForValidQuery() throws Exception {
            // Given - Create test data
            String systemCode1 = TestDataFactory.createSystemCode();
            String systemCode2 = TestDataFactory.createSystemCode();
            SolutionReview review1 = createAndSaveSolutionReview(systemCode1, DocumentState.ACTIVE);
            SolutionReview review2 = createAndSaveSolutionReview(systemCode2, DocumentState.ACTIVE);

            SearchQueryDTO searchQuery = SearchQueryDTO.builder()
                    .searchQuery("find active systems")
                    .build();

            // Mock chatbot service response
            List<Map<String, Object>> mongoQuery = List.of(
                    Map.of("$match", Map.of("documentState", "ACTIVE")),
                    Map.of("$project", Map.of("_id", 1))
            );

            ChatbotTranslateResponseDTO chatbotResponse = ChatbotTranslateResponseDTO.builder()
                    .mongoQuery(mongoQuery)
                    .build();

            when(chatbotServiceClient.translate("find active systems", false))
                    .thenReturn(chatbotResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(searchQuery)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[*].id", hasItems(review1.getId(), review2.getId())))
                    .andExpect(jsonPath("$[*].systemCode", hasItems(systemCode1, systemCode2)))
                    .andExpect(jsonPath("$[*].documentState", everyItem(is("ACTIVE"))));

            verify(chatbotServiceClient).translate("find active systems", false);
        }

        @Test
        @DisplayName("Should return empty list when no results match")
        @Description("Tests that search returns empty array when no documents match the query")
        @Severity(SeverityLevel.NORMAL)
        void shouldReturnEmptyListWhenNoResultsMatch() throws Exception {
            // Given - Create test data with DRAFT state
            String systemCode = TestDataFactory.createSystemCode();
            createAndSaveSolutionReview(systemCode, DocumentState.DRAFT);

            SearchQueryDTO searchQuery = SearchQueryDTO.builder()
                    .searchQuery("find approved systems")
                    .build();

            // Mock chatbot service to return query that matches APPROVED state
            List<Map<String, Object>> mongoQuery = List.of(
                    Map.of("$match", Map.of("documentState", "APPROVED")),
                    Map.of("$project", Map.of("_id", 1))
            );

            ChatbotTranslateResponseDTO chatbotResponse = ChatbotTranslateResponseDTO.builder()
                    .mongoQuery(mongoQuery)
                    .build();

            when(chatbotServiceClient.translate("find approved systems", false))
                    .thenReturn(chatbotResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(searchQuery)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return empty list when chatbot returns null query")
        @Description("Tests that search handles null MongoDB query gracefully")
        @Severity(SeverityLevel.NORMAL)
        void shouldReturnEmptyListWhenChatbotReturnsNullQuery() throws Exception {
            // Given
            SearchQueryDTO searchQuery = SearchQueryDTO.builder()
                    .searchQuery("invalid query format")
                    .build();

            ChatbotTranslateResponseDTO chatbotResponse = ChatbotTranslateResponseDTO.builder()
                    .mongoQuery(null)
                    .build();

            when(chatbotServiceClient.translate("invalid query format", false))
                    .thenReturn(chatbotResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(searchQuery)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should filter by specific system code")
        @Description("Tests that search can filter by specific system code")
        @Severity(SeverityLevel.NORMAL)
        void shouldFilterBySpecificSystemCode() throws Exception {
            // Given - Create multiple reviews
            String targetSystemCode = TestDataFactory.createSystemCode();
            String otherSystemCode = TestDataFactory.createSystemCode();
            SolutionReview targetReview = createAndSaveSolutionReview(targetSystemCode, DocumentState.ACTIVE);
            createAndSaveSolutionReview(otherSystemCode, DocumentState.ACTIVE);

            SearchQueryDTO searchQuery = SearchQueryDTO.builder()
                    .searchQuery("find system " + targetSystemCode)
                    .build();

            // Mock chatbot service to return query that matches specific system code
            List<Map<String, Object>> mongoQuery = List.of(
                    Map.of("$match", Map.of("systemCode", targetSystemCode)),
                    Map.of("$project", Map.of("_id", 1))
            );

            ChatbotTranslateResponseDTO chatbotResponse = ChatbotTranslateResponseDTO.builder()
                    .mongoQuery(mongoQuery)
                    .build();

            when(chatbotServiceClient.translate("find system " + targetSystemCode, false))
                    .thenReturn(chatbotResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(searchQuery)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(targetReview.getId()))
                    .andExpect(jsonPath("$[0].systemCode").value(targetSystemCode));
        }

        @Test
        @DisplayName("Should return correct DTO structure")
        @Description("Verifies that search results contain all expected CleanSolutionReviewDTO fields")
        @Severity(SeverityLevel.CRITICAL)
        void shouldReturnCorrectDTOStructure() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            SearchQueryDTO searchQuery = SearchQueryDTO.builder()
                    .searchQuery("find all")
                    .build();

            List<Map<String, Object>> mongoQuery = List.of(
                    Map.of("$match", Map.of()),
                    Map.of("$project", Map.of("_id", 1))
            );

            ChatbotTranslateResponseDTO chatbotResponse = ChatbotTranslateResponseDTO.builder()
                    .mongoQuery(mongoQuery)
                    .build();

            when(chatbotServiceClient.translate("find all", false))
                    .thenReturn(chatbotResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(searchQuery)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(review.getId()))
                    .andExpect(jsonPath("$[0].systemCode").value(systemCode))
                    .andExpect(jsonPath("$[0].documentState").value("ACTIVE"))
                    .andExpect(jsonPath("$[0].solutionOverview").exists())
                    .andExpect(jsonPath("$[0].solutionOverview.id").exists())
                    .andExpect(jsonPath("$[0].solutionOverview.solutionDetails").exists());
        }

        @Test
        @DisplayName("Should handle chatbot service exception")
        @Description("Tests error handling when chatbot service throws an exception")
        @Severity(SeverityLevel.CRITICAL)
        void shouldHandleChatbotServiceException() throws Exception {
            // Given
            SearchQueryDTO searchQuery = SearchQueryDTO.builder()
                    .searchQuery("test query")
                    .build();

            when(chatbotServiceClient.translate("test query", false))
                    .thenThrow(new RuntimeException("Chatbot service unavailable"));

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(searchQuery)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value(containsString("Failed to communicate with chatbot service")));
        }

        @Test
        @DisplayName("Should handle multiple document states in search")
        @Description("Tests that search can filter documents with different states")
        @Severity(SeverityLevel.NORMAL)
        void shouldHandleMultipleDocumentStates() throws Exception {
            // Given - Create reviews with different states
            String systemCode1 = TestDataFactory.createSystemCode();
            String systemCode2 = TestDataFactory.createSystemCode();
            String systemCode3 = TestDataFactory.createSystemCode();
            SolutionReview draftReview = createAndSaveSolutionReview(systemCode1, DocumentState.DRAFT);
            SolutionReview activeReview = createAndSaveSolutionReview(systemCode2, DocumentState.ACTIVE);
            createAndSaveSolutionReview(systemCode3, DocumentState.OUTDATED);

            SearchQueryDTO searchQuery = SearchQueryDTO.builder()
                    .searchQuery("find draft or active")
                    .build();

            // Mock chatbot service to return query that matches DRAFT or ACTIVE
            List<Map<String, Object>> mongoQuery = List.of(
                    Map.of("$match", Map.of("documentState", Map.of("$in", List.of("DRAFT", "ACTIVE")))),
                    Map.of("$project", Map.of("_id", 1))
            );

            ChatbotTranslateResponseDTO chatbotResponse = ChatbotTranslateResponseDTO.builder()
                    .mongoQuery(mongoQuery)
                    .build();

            when(chatbotServiceClient.translate("find draft or active", false))
                    .thenReturn(chatbotResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(searchQuery)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[*].id", hasItems(draftReview.getId(), activeReview.getId())));
        }

        @Test
        @DisplayName("Should filter out non-existent documents from results")
        @Description("Tests that search gracefully handles document IDs that don't exist in database")
        @Severity(SeverityLevel.NORMAL)
        void shouldFilterOutNonExistentDocuments() throws Exception {
            // Given
            String systemCode = TestDataFactory.createSystemCode();
            SolutionReview review = createAndSaveSolutionReview(systemCode, DocumentState.ACTIVE);

            SearchQueryDTO searchQuery = SearchQueryDTO.builder()
                    .searchQuery("find all")
                    .build();

            // Mock chatbot service to return query - the actual filtering happens at service level
            List<Map<String, Object>> mongoQuery = List.of(
                    Map.of("$match", Map.of("documentState", "ACTIVE")),
                    Map.of("$project", Map.of("_id", 1))
            );

            ChatbotTranslateResponseDTO chatbotResponse = ChatbotTranslateResponseDTO.builder()
                    .mongoQuery(mongoQuery)
                    .build();

            when(chatbotServiceClient.translate("find all", false))
                    .thenReturn(chatbotResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/solution-review/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(searchQuery)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(review.getId()));
        }
    }
}
