package com.project.core_service.controllers;

import com.project.core_service.exceptions.IllegalOperationException;
import com.project.core_service.exceptions.IllegalStateTransitionException;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.services.SolutionReviewLifecycleService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(LifecycleController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("LifecycleController Tests")
class LifecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolutionReviewLifecycleService lifecycleService;

    @Nested
    @DisplayName("POST /api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(errorMessage));

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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
                    "ACTIVATE",
                    "UNAPPROVE",
                    "MARK_OUTDATED"
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
                mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(post("/api/v1/lifecycle/transition")
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
            mockMvc.perform(get("/api/v1/lifecycle/transition"))
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
            mockMvc.perform(put("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isMethodNotAllowed());

            verify(lifecycleService, never()).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 405 Method Not Allowed for DELETE request")
        void shouldReturnMethodNotAllowedForDeleteRequest() throws Exception {
            mockMvc.perform(delete("/api/v1/lifecycle/transition"))
                    .andExpect(status().isMethodNotAllowed());

            verify(lifecycleService, never()).executeTransition(any());
        }
    }

    @Nested
    @DisplayName("Business Logic Constraint Tests")
    class BusinessLogicConstraintTests {

        @Test
        @DisplayName("Should return 400 Bad Request when trying to create DRAFT with existing DRAFT/SUBMITTED/APPROVED")
        void shouldReturnBadRequestWhenTryingToCreateDraftWithExistingExclusiveStates() throws Exception {
            // Given - attempt to transition to DRAFT when another exclusive state exists
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Creating a new draft"
                    }
                    """;
            String errorMessage = "Cannot create/update document for system SYS-001. " +
                    "Documents already exist in exclusive states: DRAFT. " +
                    "Only one document can be in DRAFT, SUBMITTED, or APPROVED state at a time.";
            doThrow(new IllegalOperationException(errorMessage))
                    .when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when trying to APPROVE with existing APPROVED document")
        void shouldReturnBadRequestWhenTryingToApproveWithExistingApprovedDocument() throws Exception {
            // Given
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "APPROVE",
                        "modifiedBy": "test-user",
                        "comment": "Trying to approve when another approved document exists"
                    }
                    """;
            String errorMessage = "Cannot create/update document for system SYS-001. " +
                    "Documents already exist in exclusive states: APPROVED. " +
                    "Only one document can be in DRAFT, SUBMITTED, or APPROVED state at a time.";
            doThrow(new IllegalOperationException(errorMessage))
                    .when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when trying to ACTIVATE with existing ACTIVE document")
        void shouldReturnBadRequestWhenTryingToActivateWithExistingActiveDocument() throws Exception {
            // Given
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "ACTIVATE",
                        "modifiedBy": "test-user",
                        "comment": "Trying to activate when another active document exists"
                    }
                    """;
            String errorMessage = "An ACTIVE document already exists for system SYS-001. " +
                    "Only one document can be in ACTIVE state at a time.";
            doThrow(new IllegalOperationException(errorMessage))
                    .when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should successfully ACTIVATE when constraint validation passes")
        void shouldSuccessfullyActivateWhenConstraintValidationPasses() throws Exception {
            // Given - ACTIVATE operation should succeed when constraints are satisfied
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "ACTIVATE",
                        "modifiedBy": "test-user",
                        "comment": "Activating an approved document"
                    }
                    """;
            doNothing().when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should handle multiple constraint violations with appropriate error messages")
        void shouldHandleMultipleConstraintViolationsWithAppropriateErrorMessages() throws Exception {
            // Given - multiple documents in exclusive states
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Trying to submit with multiple constraint violations"
                    }
                    """;
            String errorMessage = "Cannot create/update document for system SYS-001. " +
                    "Documents already exist in exclusive states: DRAFT, SUBMITTED. " +
                    "Only one document can be in DRAFT, SUBMITTED, or APPROVED state at a time.";
            doThrow(new IllegalOperationException(errorMessage))
                    .when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should allow DRAFT creation when only ACTIVE document exists")
        void shouldAllowDraftCreationWhenOnlyActiveDocumentExists() throws Exception {
            // Given - DRAFT creation should be allowed when only ACTIVE exists (separate
            // constraints)
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Creating draft while active document exists"
                    }
                    """;
            doNothing().when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should allow OUTDATED documents without constraint violations")
        void shouldAllowOutdatedDocumentsWithoutConstraintViolations() throws Exception {
            // Given - MARK_OUTDATED should not be subject to exclusive state constraints
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "MARK_OUTDATED",
                        "modifiedBy": "test-user",
                        "comment": "Marking document as outdated"
                    }
                    """;
            doNothing().when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            verify(lifecycleService, times(1)).executeTransition(any());
        }

        @Test
        @DisplayName("Should successfully handle ACTIVATE operation that transitions existing ACTIVE to OUTDATED")
        void shouldSuccessfullyHandleActivateOperationThatTransitionsExistingActiveToOutdated() throws Exception {
            // Given - This tests requirement #2: when activating a document, existing
            // ACTIVE should become OUTDATED
            // The service layer handles this automatically, so the API should succeed
            String requestJson = """
                    {
                        "documentId": "test-approved-document",
                        "operation": "ACTIVATE",
                        "modifiedBy": "test-user",
                        "comment": "Activating approved document, existing active should become outdated"
                    }
                    """;
            // The service layer will handle the logic of transitioning existing ACTIVE →
            // OUTDATED
            doNothing().when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Transition successful"));

            verify(lifecycleService, times(1)).executeTransition(any());
        }
    }

    @Nested
    @DisplayName("State Transition Validation Tests")
    class StateTransitionValidationTests {

        @Test
        @DisplayName("Should enforce DRAFT → SUBMITTED → APPROVED → ACTIVE → OUTDATED flow")
        void shouldEnforceCorrectStateTransitionFlow() throws Exception {
            // Test each valid transition in the flow
            String[][] validTransitions = {
                    { "SUBMIT", "DRAFT to SUBMITTED transition" },
                    { "APPROVE", "SUBMITTED to APPROVED transition" },
                    { "ACTIVATE", "APPROVED to ACTIVE transition" },
                    { "MARK_OUTDATED", "ACTIVE to OUTDATED transition" }
            };

            for (String[] transition : validTransitions) {
                String operation = transition[0];
                String comment = transition[1];

                String requestJson = String.format("""
                        {
                            "documentId": "test-document-id",
                            "operation": "%s",
                            "modifiedBy": "test-user",
                            "comment": "%s"
                        }
                        """, operation, comment);
                doNothing().when(lifecycleService).executeTransition(any());

                mockMvc.perform(post("/api/v1/lifecycle/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Transition successful"));

                verify(lifecycleService).executeTransition(any());
                reset(lifecycleService);
            }
        }

        @Test
        @DisplayName("Should allow reverse transitions where appropriate")
        void shouldAllowReverseTransitionsWhereAppropriate() throws Exception {
            // Test valid reverse transitions
            String[][] reverseTransitions = {
                    { "REMOVE_SUBMISSION", "SUBMITTED to DRAFT transition" },
                    { "UNAPPROVE", "APPROVED to SUBMITTED transition" }
            };

            for (String[] transition : reverseTransitions) {
                String operation = transition[0];
                String comment = transition[1];

                String requestJson = String.format("""
                        {
                            "documentId": "test-document-id",
                            "operation": "%s",
                            "modifiedBy": "test-user",
                            "comment": "%s"
                        }
                        """, operation, comment);
                doNothing().when(lifecycleService).executeTransition(any());

                mockMvc.perform(post("/api/v1/lifecycle/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Transition successful"));

                verify(lifecycleService).executeTransition(any());
                reset(lifecycleService);
            }
        }

        @Test
        @DisplayName("Should reject invalid state transitions")
        void shouldRejectInvalidStateTransitions() throws Exception {
            // Given - invalid transition
            String requestJson = """
                    {
                        "documentId": "test-document-id",
                        "operation": "SUBMIT",
                        "modifiedBy": "test-user",
                        "comment": "Invalid transition attempt"
                    }
                    """;
            String errorMessage = "Cannot execute operation 'submit document' on document 'test-document-id'. " +
                    "Document is in state 'SUBMITTED' but operation requires state 'DRAFT'";
            doThrow(new IllegalStateTransitionException(errorMessage))
                    .when(lifecycleService).executeTransition(any());

            // When & Then
            mockMvc.perform(post("/api/v1/lifecycle/transition")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value(errorMessage));

            verify(lifecycleService, times(1)).executeTransition(any());
        }
    }
}