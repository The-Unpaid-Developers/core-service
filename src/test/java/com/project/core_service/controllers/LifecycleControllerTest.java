package com.project.core_service.controllers;

import com.project.core_service.exceptions.IllegalStateTransitionException;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.service.SolutionReviewLifecycleService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(LifecycleController.class)
@DisplayName("LifecycleController Tests")
class LifecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolutionReviewLifecycleService lifecycleService;

    @Nested
    @DisplayName("POST /v1/lifecycle/transition")
    class TransitionEndpoint {

        @Test
        @DisplayName("Should return 200 OK when transition is successful")
        void shouldReturnOkWhenTransitionIsSuccessful() throws Exception {
            // Given
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Test comment"
                    }
                    """;
            doNothing().when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 404 Not Found when NotFoundException is thrown")
        void shouldReturnNotFoundWhenNotFoundExceptionIsThrown() throws Exception {
            // Given
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Test comment"
                    }
                    """;
            String errorMessage = "SolutionReview with ID 'test-document-id' not found";
            doThrow(new NotFoundException(errorMessage))
                    .when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("id " + errorMessage + " not found"));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when IllegalStateTransitionException is thrown")
        void shouldReturnBadRequestWhenIllegalStateTransitionExceptionIsThrown() throws Exception {
            // Given
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Test comment"
                    }
                    """;
            String errorMessage = "Cannot execute operation 'SUBMIT' on document 'test-document-id'. Document is in state 'SUBMITTED'";
            doThrow(new IllegalStateTransitionException(errorMessage))
                    .when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 500 Internal Server Error when generic Exception is thrown")
        void shouldReturnInternalServerErrorWhenGenericExceptionIsThrown() throws Exception {
            // Given
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Test comment"
                    }
                    """;
            String errorMessage = "Database connection failed";
            doThrow(new RuntimeException(errorMessage))
                    .when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request body is invalid JSON")
        void shouldReturnBadRequestWhenRequestBodyIsInvalidJson() throws Exception {
            // Given
            String invalidJson = "{ invalid json }";

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request body is missing required fields")
        void shouldReturnBadRequestWhenRequestBodyIsMissingRequiredFields() throws Exception {
            // Given - command with null documentId
            String requestJson = """
                    {
                        "documentId": null,
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Test comment"
                    }
                    """;

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Document ID is required"));

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when documentId is blank")
        void shouldReturnBadRequestWhenDocumentIdIsBlank() throws Exception {
            // Given - command with blank documentId
            String requestJson = """
                    {
                        "documentId": "",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Test comment"
                    }
                    """;

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Document ID is required"));

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when operation is null")
        void shouldReturnBadRequestWhenOperationIsNull() throws Exception {
            // Given - command with null operation
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": null,
                        "modifiedBy": "test-user",
                        "comment": "Test comment"
                    }
                    """;

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Operation is required"));

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when modifiedBy is blank")
        void shouldReturnBadRequestWhenModifiedByIsBlank() throws Exception {
            // Given - command with blank modifiedBy
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "",
                        "comment": "Test comment"
                    }
                    """;

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Modified by user is required"));

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when multiple fields are invalid")
        void shouldReturnBadRequestWhenMultipleFieldsAreInvalid() throws Exception {
            // Given - command with multiple validation errors
            String requestJson = """
                    {
                        "documentId": "",
                        "operation": null,
                        "modifiedBy": "",
                        "comment": "Test comment"
                    }
                    """;

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists()); // Should contain multiple validation messages

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 415 Unsupported Media Type when Content-Type is not JSON")
        void shouldReturnUnsupportedMediaTypeWhenContentTypeIsNotJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("plain text content"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should handle all DocumentState operations correctly")
        void shouldHandleAllDocumentStateOperationsCorrectly() throws Exception {
            // Test each operation
            String[] operations = {
                    "SUBMIT",
                    "REMOVE_SUBMISSION",
                    "APPROVE",
                    "UNAPPROVE",
                    "MARK_OUTDATED",
                    "RESET_CURRENT"
            };

            for (String operation : operations) {
                // Given
                String requestJson = String.format("""
                        {
                            "documentId": "test-document-id",
                            "operation": "%s",
                            "modifiedBy": "test-user",
                            "comment": "Test comment for %s"
                        }
                        """, operation, operation);
                doNothing().when(lifecycleService).executeTransition(any());

                // When & Then
                mockMvc.perform(post("/v1/lifecycle/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Transition successful"));

                verify(lifecycleService).executeTransition(any());

                // Reset mock for next iteration
                reset(lifecycleService);
            }
        }

        @Test
        @DisplayName("Should handle command without comment")
        void shouldHandleCommandWithoutComment() throws Exception {
            // Given - command without comment
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": null
                    }
                    """;
            doNothing().when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws Exception {
            // When & Then
            mockMvc.perform(post("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                    .andExpect(status().isBadRequest());

            verify(lifecycleService, never()).executeTransition(any());
        }
    }

    @Nested
    @DisplayName("HTTP Method Tests")
    class HttpMethodTests {

        @Test
        @DisplayName("Should return 405 Method Not Allowed for GET request")
        void shouldReturnMethodNotAllowedForGetRequest() throws Exception {
            mockMvc.perform(get("/v1/lifecycle/transition"))
                    .andExpect(status().isMethodNotAllowed());

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 405 Method Not Allowed for PUT request")
        void shouldReturnMethodNotAllowedForPutRequest() throws Exception {
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Test comment"
                    }
                    """;
            mockMvc.perform(put("/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isMethodNotAllowed());

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 405 Method Not Allowed for DELETE request")
        void shouldReturnMethodNotAllowedForDeleteRequest() throws Exception {
            mockMvc.perform(delete("/v1/lifecycle/transition"))
                    .andExpect(status().isMethodNotAllowed());

            verify(lifecycleService, never()).executeTransition(any());
        }
    }
}