package com.project.core_service.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.core_service.dto.CleanSolutionReviewDTO;
import com.project.core_service.dto.NewSolutionOverviewRequestDTO;
import com.project.core_service.dto.SolutionReviewDTO;
import com.project.core_service.dto.SystemDependencyDTO;
import com.project.core_service.dto.BusinessCapabilityDiagramDTO;
import com.project.core_service.exceptions.IllegalOperationException;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.integration_flow.CounterpartSystemRole;
import com.project.core_service.models.integration_flow.IntegrationFlow;
import com.project.core_service.models.integration_flow.IntegrationMethod;
import com.project.core_service.models.integration_flow.Middleware;
import com.project.core_service.models.shared.Frequency;
import com.project.core_service.models.solution_overview.*;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class SolutionReviewServiceTest {

    @Mock
    private SolutionReviewRepository solutionReviewRepository;
    @Mock
    private com.project.core_service.client.ChatbotServiceClient chatbotServiceClient;
    @InjectMocks
    private SolutionReviewService service;

    private SolutionReview review;
    private SolutionOverview overview;

    private SolutionDetails dummySolutionDetails() {
        return new SolutionDetails(
                "SolutionName",
                "ProjectName",
                "AWG001",
                "Architect",
                "PM",
                "Partner1");
    }

    private List<ApplicationUser> dummyApplicationUsers() {
        return List.of(ApplicationUser.EMPLOYEE, ApplicationUser.CUSTOMERS);
    }

    private List<Concern> dummyConcerns() {
        Concern dummyConcern = new Concern(
                "concern-001",
                ConcernType.RISK,
                "desc",
                "impact",
                "disposition",
                ConcernStatus.UNKNOWN,
                LocalDateTime.of(2025, 10, 25, 14, 15, 46, 372370000));
        return List.of(dummyConcern);
    }

    @BeforeEach
    void setup() {
        overview = new SolutionOverview(
                "id-001",
                dummySolutionDetails(),
                "ReviewerName",
                ReviewType.NEW_BUILD,
                "No conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.OPERATIONAL_EFFICIENCY,
                "Expected value outcome",
                dummyApplicationUsers(),
                dummyConcerns());

        review = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getSolutionReviewById_Found() {
        when(solutionReviewRepository.findById("rev-1")).thenReturn(Optional.of(review));

        Optional<SolutionReview> result = service.getSolutionReviewById("rev-1");

        assertTrue(result.isPresent());
        assertEquals("rev-1", result.get().getId());
        verify(solutionReviewRepository).findById("rev-1");
    }

    @Test
    void getSolutionReviewById_NotFound() {
        when(solutionReviewRepository.findById("x")).thenReturn(Optional.empty());

        Optional<SolutionReview> result = service.getSolutionReviewById("x");

        assertTrue(result.isEmpty());
    }

    @Test
    void getSolutionReviews_Page() {
        Page<SolutionReview> page = new PageImpl<>(List.of(review));
        when(solutionReviewRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<CleanSolutionReviewDTO> result = service.getSolutionReviews(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getSolutionReviewsBySystemCode() {
        when(solutionReviewRepository.findAllBySystemCode("SYS-123"))
                .thenReturn(List.of(review));

        List<CleanSolutionReviewDTO> result = service.getSolutionReviewsBySystemCode("SYS-123");

        assertEquals(1, result.size());
        assertEquals("SYS-123", result.get(0).getSystemCode());
    }

    @Test
    void getAllSolutionReviews() {
        when(solutionReviewRepository.findAll(any(Sort.class))).thenReturn(List.of(review));

        List<CleanSolutionReviewDTO> result = service.getAllSolutionReviews();

        assertEquals(1, result.size());
    }

    @Test
    void getPaginatedSystemView_ShouldReturnActiveReviewWhenExists() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123", "SYS-456");

        SolutionReview activeReview = SolutionReview.newDraftBuilder()
                .id("rev-active")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findActiveBySystemCode("SYS-123"))
                .thenReturn(Optional.of(activeReview));
        when(solutionReviewRepository.findActiveBySystemCode("SYS-456"))
                .thenReturn(Optional.empty());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-456"), any(Sort.class))).thenReturn(List.of());

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("SYS-123", result.getContent().get(0).getSystemCode());
        assertEquals(DocumentState.ACTIVE, result.getContent().get(0).getDocumentState());
    }

    @Test
    void getPaginatedSystemView_ShouldReturnLatestReviewWhenNoActiveExists() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123");

        SolutionReview latestReview = SolutionReview.newDraftBuilder()
                .id("rev-latest")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .lastModifiedAt(LocalDateTime.now())
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findActiveBySystemCode("SYS-123")).thenReturn(Optional.empty());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(latestReview));

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("SYS-123", result.getContent().get(0).getSystemCode());
        assertEquals(DocumentState.DRAFT, result.getContent().get(0).getDocumentState());
    }

    @Test
    void getPaginatedSystemView_ShouldFilterOutSystemsWithNoReviews() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123", "SYS-456");

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findActiveBySystemCode("SYS-123")).thenReturn(Optional.empty());
        when(solutionReviewRepository.findActiveBySystemCode("SYS-456")).thenReturn(Optional.empty());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-123"), any(Sort.class))).thenReturn(List.of());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-456"), any(Sort.class))).thenReturn(List.of());

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getPaginatedSystemView_ShouldHandlePaginationCorrectly() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2); // Second page, 2 items per page
        List<String> systemCodes = List.of("SYS-001", "SYS-002", "SYS-003", "SYS-004", "SYS-005");

        // Create 5 reviews for different systems
        for (int i = 1; i <= 5; i++) {
            SolutionReview reviews = SolutionReview.newDraftBuilder()
                    .id("rev-" + i)
                    .systemCode("SYS-00" + i)
                    .solutionOverview(overview)
                    .documentState(DocumentState.ACTIVE)
                    .build();
            when(solutionReviewRepository.findActiveBySystemCode("SYS-00" + i))
                    .thenReturn(Optional.of(reviews));
        }

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(5, result.getTotalElements()); // Total across all pages
        assertEquals(2, result.getContent().size()); // Current page size
        assertEquals(1, result.getNumber()); // Current page number (0-indexed)
        assertEquals(3, result.getTotalPages()); // Total pages (5 items, 2 per page = 3 pages)

        // Verify we got the correct items for page 1 (items 3 and 4)
        assertEquals("SYS-003", result.getContent().get(0).getSystemCode());
        assertEquals("SYS-004", result.getContent().get(1).getSystemCode());
    }

    @Test
    void getPaginatedSystemView_ShouldHandleEmptySystemCodes() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getPaginatedSystemView_ShouldHandleOffsetBeyondAvailableData() {
        // Arrange
        Pageable pageable = PageRequest.of(10, 10); // Page 10 with 10 items per page
        List<String> systemCodes = List.of("SYS-123");

        SolutionReview currentReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findActiveBySystemCode("SYS-123"))
                .thenReturn(Optional.of(currentReview));

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(1, result.getTotalElements()); // Total items available
        assertTrue(result.getContent().isEmpty()); // No items on this page
        assertEquals(10, result.getNumber()); // Requested page number
    }

    @Test
    void getPaginatedSystemView_ShouldPrioritizeActiveOverOtherStates() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123");

        SolutionReview activeReview = SolutionReview.newDraftBuilder()
                .id("rev-active")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .lastModifiedAt(LocalDateTime.now().minusDays(1)) // Older
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findActiveBySystemCode("SYS-123"))
                .thenReturn(Optional.of(activeReview));

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(DocumentState.ACTIVE, result.getContent().get(0).getDocumentState());
        assertEquals("rev-active", result.getContent().get(0).getId());
    }

    @Test
    void getPaginatedSystemView_ShouldReturnLatestWhenMultipleNonActiveReviews() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123");

        SolutionReview olderReview = SolutionReview.newDraftBuilder()
                .id("rev-older")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.OUTDATED)
                .lastModifiedAt(LocalDateTime.now().minusDays(2))
                .build();

        SolutionReview newerReview = SolutionReview.newDraftBuilder()
                .id("rev-newer")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .lastModifiedAt(LocalDateTime.now())
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findActiveBySystemCode("SYS-123")).thenReturn(Optional.empty());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(newerReview, olderReview)); // Sorted by lastModifiedAt DESC

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("rev-newer", result.getContent().get(0).getId());
        assertEquals(DocumentState.DRAFT, result.getContent().get(0).getDocumentState());
    }

    @Test
    void getPaginatedSystemView_ShouldHandleMixedActiveAndNonActiveReviews() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123", "SYS-456", "SYS-789");

        // SYS-123 has ACTIVE review
        SolutionReview activeReview = SolutionReview.newDraftBuilder()
                .id("rev-active")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        // SYS-456 has no ACTIVE, but has DRAFT (latest)
        SolutionReview draftReview = SolutionReview.newDraftBuilder()
                .id("rev-draft")
                .systemCode("SYS-456")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .lastModifiedAt(LocalDateTime.now())
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findActiveBySystemCode("SYS-123"))
                .thenReturn(Optional.of(activeReview));
        when(solutionReviewRepository.findActiveBySystemCode("SYS-456"))
                .thenReturn(Optional.empty());
        when(solutionReviewRepository.findActiveBySystemCode("SYS-789"))
                .thenReturn(Optional.empty());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-456"), any(Sort.class)))
                .thenReturn(List.of(draftReview));
        when(solutionReviewRepository.findBySystemCode(eq("SYS-789"), any(Sort.class)))
                .thenReturn(List.of()); // No reviews

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(2, result.getTotalElements()); // Only SYS-123 and SYS-456 have reviews
        assertEquals(2, result.getContent().size());

        // Find reviews by system code
        CleanSolutionReviewDTO sys123Result = result.getContent().stream()
                .filter(r -> "SYS-123".equals(r.getSystemCode()))
                .findFirst().orElse(null);
        CleanSolutionReviewDTO sys456Result = result.getContent().stream()
                .filter(r -> "SYS-456".equals(r.getSystemCode()))
                .findFirst().orElse(null);

        assertNotNull(sys123Result);
        assertEquals(DocumentState.ACTIVE, sys123Result.getDocumentState());

        assertNotNull(sys456Result);
        assertEquals(DocumentState.DRAFT, sys456Result.getDocumentState());
    }

    @Test
    void getPaginatedSystemView_ShouldHandleVeryLargePageNumber() {
        // Arrange
        Pageable pageable = PageRequest.of(1000, 10);
        List<String> systemCodes = List.of("SYS-123");

        SolutionReview currentReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findActiveBySystemCode("SYS-123"))
                .thenReturn(Optional.of(currentReview));

        // Act
        Page<CleanSolutionReviewDTO> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().isEmpty()); // No items on this high page number
        assertEquals(1000, result.getNumber());
    }

    // Tests for getSolutionReviewsByDocumentState method (now accepts String)
    @Test
    void getSolutionReviewsByDocumentState_ShouldReturnFilteredResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String documentStateStr = "DRAFT";

        SolutionReview draftReview1 = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .build();

        SolutionReview draftReview2 = SolutionReview.newDraftBuilder()
                .id("rev-2")
                .systemCode("SYS-456")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .build();

        Page<SolutionReview> expectedPage = new PageImpl<>(List.of(draftReview1, draftReview2), pageable, 2);
        when(solutionReviewRepository.findByDocumentState(DocumentState.DRAFT, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<CleanSolutionReviewDTO> result = service.getSolutionReviewsByDocumentState(documentStateStr, pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(r -> r.getDocumentState() == DocumentState.DRAFT));
        verify(solutionReviewRepository).findByDocumentState(DocumentState.DRAFT, pageable);
    }

    @Test
    void getSolutionReviewsByDocumentState_ShouldReturnEmptyWhenNoMatches() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String documentStateStr = "APPROVED";

        Page<SolutionReview> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(solutionReviewRepository.findByDocumentState(DocumentState.APPROVED, pageable))
                .thenReturn(emptyPage);

        // Act
        Page<CleanSolutionReviewDTO> result = service.getSolutionReviewsByDocumentState(documentStateStr, pageable);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(solutionReviewRepository).findByDocumentState(DocumentState.APPROVED, pageable);
    }

    @Test
    void getSolutionReviewsByDocumentState_ShouldHandleDifferentDocumentStates() {
        // Arrange - test with each document state
        DocumentState[] states = DocumentState.values();
        Pageable pageable = PageRequest.of(0, 10);

        for (DocumentState state : states) {
            SolutionReview reviewWithState = SolutionReview.newDraftBuilder()
                    .id("rev-" + state)
                    .systemCode("SYS-123")
                    .solutionOverview(overview)
                    .documentState(state)
                    .build();

            Page<SolutionReview> page = new PageImpl<>(List.of(reviewWithState), pageable, 1);
            when(solutionReviewRepository.findByDocumentState(state, pageable)).thenReturn(page);

            // Act
            Page<CleanSolutionReviewDTO> result = service.getSolutionReviewsByDocumentState(state.name(), pageable);

            // Assert
            assertEquals(1, result.getTotalElements());
            assertEquals(state, result.getContent().get(0).getDocumentState());
        }
    }

    @Test
    void getSolutionReviewsByDocumentState_ShouldThrowExceptionForInvalidState() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String invalidState = "INVALID_STATE";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.getSolutionReviewsByDocumentState(invalidState, pageable));

        assertTrue(exception.getMessage().contains("Invalid document state: INVALID_STATE"));
        assertTrue(exception.getMessage().contains("Valid values:"));
    }

    @Test
    void getSolutionReviewsByDocumentState_ShouldHandleCaseInsensitivity() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String lowerCaseState = "draft";

        SolutionReview draftReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .build();

        Page<SolutionReview> expectedPage = new PageImpl<>(List.of(draftReview), pageable, 1);
        when(solutionReviewRepository.findByDocumentState(DocumentState.DRAFT, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<CleanSolutionReviewDTO> result = service.getSolutionReviewsByDocumentState(lowerCaseState, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(DocumentState.DRAFT, result.getContent().get(0).getDocumentState());
        verify(solutionReviewRepository).findByDocumentState(DocumentState.DRAFT, pageable);
    }

    @Test
    void getSolutionReviewsByDocumentState_ShouldHandlePagination() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 5); // Second page, 5 items per page
        String documentStateStr = "DRAFT";

        List<SolutionReview> pageContent = List.of(
                SolutionReview.newDraftBuilder().id("rev-6").systemCode("SYS-006")
                        .solutionOverview(overview).documentState(DocumentState.DRAFT).build(),
                SolutionReview.newDraftBuilder().id("rev-7").systemCode("SYS-007")
                        .solutionOverview(overview).documentState(DocumentState.DRAFT).build());

        Page<SolutionReview> expectedPage = new PageImpl<>(pageContent, pageable, 12); // 12 total items
        when(solutionReviewRepository.findByDocumentState(DocumentState.DRAFT, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<CleanSolutionReviewDTO> result = service.getSolutionReviewsByDocumentState(documentStateStr, pageable);

        // Assert
        assertEquals(12, result.getTotalElements()); // Total items
        assertEquals(2, result.getContent().size()); // Items on current page
        assertEquals(1, result.getNumber()); // Current page number
        assertEquals(3, result.getTotalPages()); // Total pages (12 items, 5 per page = 3 pages)
        verify(solutionReviewRepository).findByDocumentState(DocumentState.DRAFT, pageable);
    }

    @Test
    void getSolutionReviewsByDocumentState_ShouldHandleNullAndEmptyStrings() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert - Null string
        IllegalArgumentException nullException = assertThrows(IllegalArgumentException.class,
                () -> service.getSolutionReviewsByDocumentState(null, pageable));
        assertTrue(nullException.getMessage().contains("Invalid document state: null"));

        // Act & Assert - Empty string
        IllegalArgumentException emptyException = assertThrows(IllegalArgumentException.class,
                () -> service.getSolutionReviewsByDocumentState("", pageable));
        assertTrue(emptyException.getMessage().contains("Invalid document state:"));

        // Act & Assert - Blank string
        IllegalArgumentException blankException = assertThrows(IllegalArgumentException.class,
                () -> service.getSolutionReviewsByDocumentState("   ", pageable));
        assertTrue(blankException.getMessage().contains("Invalid document state:"));
    }

    @Test
    void getSolutionReviewsByDocumentState_ShouldDelegateToRepository() {
        // Arrange - This test verifies that the service method delegates to repository
        // after validation
        Pageable pageable = PageRequest.of(0, 10);
        String documentStateStr = "SUBMITTED";

        SolutionReview submittedReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.SUBMITTED)
                .build();

        Page<SolutionReview> mockPage = new PageImpl<>(List.of(submittedReview), pageable, 1);
        when(solutionReviewRepository.findByDocumentState(DocumentState.SUBMITTED, pageable))
                .thenReturn(mockPage);

        // Act
        Page<CleanSolutionReviewDTO> result = service.getSolutionReviewsByDocumentState(documentStateStr, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("SYS-123", result.getContent().get(0).getSystemCode());
        assertEquals(DocumentState.SUBMITTED, result.getContent().get(0).getDocumentState());
        verify(solutionReviewRepository).findByDocumentState(DocumentState.SUBMITTED, pageable);
    }

    // Tests for getSystemDependencySolutionReviews method
    @Test
    void getSystemDependencySolutionReviews_ShouldReturnActiveSolutionReviews() {
        // Arrange
        SolutionReview activeReview1 = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .integrationFlows(List.of())
                .build();

        SolutionReview activeReview2 = SolutionReview.newDraftBuilder()
                .id("rev-2")
                .systemCode("SYS-456")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .integrationFlows(List.of())
                .build();

        List<SolutionReview> activeSolutionReviews = List.of(activeReview1, activeReview2);
        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(activeSolutionReviews);

        // Act
        List<SystemDependencyDTO> result = service.getSystemDependencySolutionReviews();

        // Assert
        assertEquals(2, result.size());

        SystemDependencyDTO dto1 = result.get(0);
        assertEquals("SYS-123", dto1.getSystemCode());
        assertEquals(overview, dto1.getSolutionOverview());
        assertEquals(List.of(), dto1.getIntegrationFlows());

        SystemDependencyDTO dto2 = result.get(1);
        assertEquals("SYS-456", dto2.getSystemCode());
        assertEquals(overview, dto2.getSolutionOverview());
        assertEquals(List.of(), dto2.getIntegrationFlows());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getSystemDependencySolutionReviews_ShouldReturnEmptyListWhenNoActiveReviews() {
        // Arrange
        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE)).thenReturn(List.of());

        // Act
        List<SystemDependencyDTO> result = service.getSystemDependencySolutionReviews();

        // Assert
        assertTrue(result.isEmpty());
        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getSystemDependencySolutionReviews_ShouldIncludeIntegrationFlowsWhenPresent() {
        // Arrange
        List<IntegrationFlow> integrationFlows = List.of(
                IntegrationFlow.builder()
                        .id("if-1")
                        .componentName("Component1")
                        .counterpartSystemCode("EXT-001")
                        .counterpartSystemRole(CounterpartSystemRole.CONSUMER)
                        .integrationMethod(IntegrationMethod.API)
                        .frequency(Frequency.DAILY)
                        .purpose("Data sync")
                        .middleware(Middleware.API_GATEWAY)
                        .build());

        SolutionReview activeReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-789")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .integrationFlows(integrationFlows)
                .build();

        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(activeReview));

        // Act
        List<SystemDependencyDTO> result = service.getSystemDependencySolutionReviews();

        // Assert
        assertEquals(1, result.size());
        SystemDependencyDTO dto = result.get(0);
        assertEquals("SYS-789", dto.getSystemCode());
        assertEquals(overview, dto.getSolutionOverview());
        assertEquals(integrationFlows, dto.getIntegrationFlows());
        assertEquals(1, dto.getIntegrationFlows().size());
        assertEquals("if-1", dto.getIntegrationFlows().get(0).getId());
        assertEquals("Component1", dto.getIntegrationFlows().get(0).getComponentName());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getSystemDependencySolutionReviews_ShouldOnlyReturnActiveDocumentState() {
        // Arrange
        SolutionReview activeReview = SolutionReview.newDraftBuilder()
                .id("rev-active")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        // Create reviews with other document states (these should not be returned)
        // We don't need to store these in variables as they're only for documentation

        // Only return the active review from repository
        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(activeReview));

        // Act
        List<SystemDependencyDTO> result = service.getSystemDependencySolutionReviews();

        // Assert
        assertEquals(1, result.size());
        assertEquals("SYS-123", result.get(0).getSystemCode());

        // Verify only ACTIVE state was queried
        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
        verify(solutionReviewRepository, never()).findByDocumentState(DocumentState.DRAFT);
        verify(solutionReviewRepository, never()).findByDocumentState(DocumentState.APPROVED);
        verify(solutionReviewRepository, never()).findByDocumentState(DocumentState.SUBMITTED);
        verify(solutionReviewRepository, never()).findByDocumentState(DocumentState.OUTDATED);
    }

    @Test
    void getSystemDependencySolutionReviews_ShouldHandleMultipleActiveReviewsWithDifferentData() {
        // Arrange
        List<IntegrationFlow> integrationFlows1 = List.of(
                IntegrationFlow.builder()
                        .id("if-1")
                        .componentName("Component1")
                        .counterpartSystemCode("EXT-001")
                        .counterpartSystemRole(CounterpartSystemRole.CONSUMER)
                        .integrationMethod(IntegrationMethod.API)
                        .frequency(Frequency.DAILY)
                        .purpose("Data sync")
                        .middleware(Middleware.API_GATEWAY)
                        .build());

        List<IntegrationFlow> integrationFlows2 = List.of(
                IntegrationFlow.builder()
                        .id("if-2")
                        .componentName("Component2")
                        .counterpartSystemCode("EXT-002")
                        .counterpartSystemRole(CounterpartSystemRole.PRODUCER)
                        .integrationMethod(IntegrationMethod.BATCH)
                        .frequency(Frequency.WEEKLY)
                        .purpose("Report generation")
                        .middleware(Middleware.OSB)
                        .build(),
                IntegrationFlow.builder()
                        .id("if-3")
                        .componentName("Component3")
                        .counterpartSystemCode("EXT-003")
                        .counterpartSystemRole(CounterpartSystemRole.CONSUMER)
                        .integrationMethod(IntegrationMethod.EVENT)
                        .frequency(Frequency.MONTHLY)
                        .purpose("Event processing")
                        .middleware(Middleware.NONE)
                        .build());

        SolutionOverview overview2 = SolutionOverview.newDraftBuilder()
                .id("id-002")
                .solutionDetails(dummySolutionDetails())
                .reviewedBy("ReviewerName2")
                .businessUnit(BusinessUnit.UNKNOWN)
                .businessDriver(BusinessDriver.REGULATORY)
                .valueOutcome("Different outcome")
                .applicationUsers(dummyApplicationUsers())
                .concerns(dummyConcerns())
                .build();

        SolutionReview activeReview1 = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .integrationFlows(integrationFlows1)
                .build();

        SolutionReview activeReview2 = SolutionReview.newDraftBuilder()
                .id("rev-2")
                .systemCode("SYS-456")
                .solutionOverview(overview2)
                .documentState(DocumentState.ACTIVE)
                .integrationFlows(integrationFlows2)
                .build();

        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(activeReview1, activeReview2));

        // Act
        List<SystemDependencyDTO> result = service.getSystemDependencySolutionReviews();

        // Assert
        assertEquals(2, result.size());

        // First review
        SystemDependencyDTO dto1 = result.get(0);
        assertEquals("SYS-123", dto1.getSystemCode());
        assertEquals(overview, dto1.getSolutionOverview());
        assertEquals(1, dto1.getIntegrationFlows().size());
        assertEquals("if-1", dto1.getIntegrationFlows().get(0).getId());

        // Second review
        SystemDependencyDTO dto2 = result.get(1);
        assertEquals("SYS-456", dto2.getSystemCode());
        assertEquals(overview2, dto2.getSolutionOverview());
        assertEquals(2, dto2.getIntegrationFlows().size());
        assertEquals("if-2", dto2.getIntegrationFlows().get(0).getId());
        assertEquals("if-3", dto2.getIntegrationFlows().get(1).getId());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getSystemDependencySolutionReviews_ShouldPreserveOrderFromRepository() {
        // Arrange
        SolutionReview review1 = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-AAA")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        SolutionReview review2 = SolutionReview.newDraftBuilder()
                .id("rev-2")
                .systemCode("SYS-ZZZ")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        SolutionReview review3 = SolutionReview.newDraftBuilder()
                .id("rev-3")
                .systemCode("SYS-MMM")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        // Repository returns in specific order
        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(review2, review1, review3)); // ZZZ, AAA, MMM

        // Act
        List<SystemDependencyDTO> result = service.getSystemDependencySolutionReviews();

        // Assert
        assertEquals(3, result.size());
        assertEquals("SYS-ZZZ", result.get(0).getSystemCode());
        assertEquals("SYS-AAA", result.get(1).getSystemCode());
        assertEquals("SYS-MMM", result.get(2).getSystemCode());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getSystemDependencySolutionReviews_ShouldUseCorrectFactoryMethod() {
        // Arrange
        SolutionReview activeReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .integrationFlows(List.of())
                .build();

        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(activeReview));

        // Act
        List<SystemDependencyDTO> result = service.getSystemDependencySolutionReviews();

        // Assert
        assertEquals(1, result.size());
        SystemDependencyDTO dto = result.get(0);

        // Verify all three required fields are properly mapped
        assertEquals(activeReview.getSystemCode(), dto.getSystemCode());
        assertEquals(activeReview.getSolutionOverview(), dto.getSolutionOverview());
        assertEquals(activeReview.getIntegrationFlows(), dto.getIntegrationFlows());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    // Tests for getBusinessCapabilitySolutionReviews method
    @Test
    void getBusinessCapabilitySolutionReviews_ShouldReturnActiveSolutionReviews() {
        // Arrange
        SolutionReview activeReview1 = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .businessCapabilities(List.of())
                .build();

        SolutionReview activeReview2 = SolutionReview.newDraftBuilder()
                .id("rev-2")
                .systemCode("SYS-456")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .businessCapabilities(List.of())
                .build();

        List<SolutionReview> activeSolutionReviews = List.of(activeReview1, activeReview2);
        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(activeSolutionReviews);

        // Act
        List<BusinessCapabilityDiagramDTO> result = service.getBusinessCapabilitySolutionReviews();

        // Assert
        assertEquals(2, result.size());

        BusinessCapabilityDiagramDTO dto1 = result.get(0);
        assertEquals("SYS-123", dto1.getSystemCode());
        assertEquals(overview, dto1.getSolutionOverview());
        assertEquals(List.of(), dto1.getBusinessCapabilities());

        BusinessCapabilityDiagramDTO dto2 = result.get(1);
        assertEquals("SYS-456", dto2.getSystemCode());
        assertEquals(overview, dto2.getSolutionOverview());
        assertEquals(List.of(), dto2.getBusinessCapabilities());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getBusinessCapabilitySolutionReviews_ShouldReturnEmptyListWhenNoActiveReviews() {
        // Arrange
        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE)).thenReturn(List.of());

        // Act
        List<BusinessCapabilityDiagramDTO> result = service.getBusinessCapabilitySolutionReviews();

        // Assert
        assertTrue(result.isEmpty());
        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getBusinessCapabilitySolutionReviews_ShouldIncludeBusinessCapabilitiesWhenPresent() {
        // Arrange
        List<BusinessCapability> businessCapabilities = List.of(
                BusinessCapability.builder()
                        .id("bc-1")
                        .l1Capability("UNKNOWN")
                        .l2Capability("UNKNOWN")
                        .l3Capability("UNKNOWN")
                        .remarks("Customer management capabilities")
                        .build(),
                BusinessCapability.builder()
                        .id("bc-2")
                        .l1Capability("UNKNOWN")
                        .l2Capability("UNKNOWN")
                        .l3Capability("UNKNOWN")
                        .remarks("Order processing capabilities")
                        .build());

        SolutionReview activeReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-789")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .businessCapabilities(businessCapabilities)
                .build();

        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(activeReview));

        // Act
        List<BusinessCapabilityDiagramDTO> result = service.getBusinessCapabilitySolutionReviews();

        // Assert
        assertEquals(1, result.size());
        BusinessCapabilityDiagramDTO dto = result.get(0);
        assertEquals("SYS-789", dto.getSystemCode());
        assertEquals(overview, dto.getSolutionOverview());
        assertEquals(businessCapabilities, dto.getBusinessCapabilities());
        assertEquals(2, dto.getBusinessCapabilities().size());
        assertEquals("bc-1", dto.getBusinessCapabilities().get(0).getId());
        assertEquals("Customer management capabilities", dto.getBusinessCapabilities().get(0).getRemarks());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getBusinessCapabilitySolutionReviews_ShouldOnlyReturnActiveDocumentState() {
        // Arrange
        SolutionReview activeReview = SolutionReview.newDraftBuilder()
                .id("rev-active")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        // Create reviews with other document states (these should not be returned)
        // We don't need to store these in variables as they're only for documentation

        // Only return the active review from repository
        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(activeReview));

        // Act
        List<BusinessCapabilityDiagramDTO> result = service.getBusinessCapabilitySolutionReviews();

        // Assert
        assertEquals(1, result.size());
        assertEquals("SYS-123", result.get(0).getSystemCode());

        // Verify only ACTIVE state was queried
        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
        verify(solutionReviewRepository, never()).findByDocumentState(DocumentState.DRAFT);
        verify(solutionReviewRepository, never()).findByDocumentState(DocumentState.APPROVED);
        verify(solutionReviewRepository, never()).findByDocumentState(DocumentState.SUBMITTED);
        verify(solutionReviewRepository, never()).findByDocumentState(DocumentState.OUTDATED);
    }

    @Test
    void getBusinessCapabilitySolutionReviews_ShouldHandleMultipleActiveReviewsWithDifferentData() {
        // Arrange
        List<BusinessCapability> businessCapabilities1 = List.of(
                BusinessCapability.builder()
                        .id("bc-1")
                        .l1Capability("UNKNOWN")
                        .l2Capability("UNKNOWN")
                        .l3Capability("UNKNOWN")
                        .remarks("Customer management capabilities")
                        .build());

        List<BusinessCapability> businessCapabilities2 = List.of(
                BusinessCapability.builder()
                        .id("bc-2")
                        .l1Capability("UNKNOWN")
                        .l2Capability("UNKNOWN")
                        .l3Capability("UNKNOWN")
                        .remarks("Order processing capabilities")
                        .build(),
                BusinessCapability.builder()
                        .id("bc-3")
                        .l1Capability("UNKNOWN")
                        .l2Capability("UNKNOWN")
                        .l3Capability("UNKNOWN")
                        .remarks("Inventory management capabilities")
                        .build());

        SolutionOverview overview2 = SolutionOverview.newDraftBuilder()
                .id("id-002")
                .solutionDetails(dummySolutionDetails())
                .reviewedBy("ReviewerName2")
                .businessUnit(BusinessUnit.UNKNOWN)
                .businessDriver(BusinessDriver.REGULATORY)
                .valueOutcome("Different outcome")
                .applicationUsers(dummyApplicationUsers())
                .concerns(dummyConcerns())
                .build();

        SolutionReview activeReview1 = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .businessCapabilities(businessCapabilities1)
                .build();

        SolutionReview activeReview2 = SolutionReview.newDraftBuilder()
                .id("rev-2")
                .systemCode("SYS-456")
                .solutionOverview(overview2)
                .documentState(DocumentState.ACTIVE)
                .businessCapabilities(businessCapabilities2)
                .build();

        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(activeReview1, activeReview2));

        // Act
        List<BusinessCapabilityDiagramDTO> result = service.getBusinessCapabilitySolutionReviews();

        // Assert
        assertEquals(2, result.size());

        // First review
        BusinessCapabilityDiagramDTO dto1 = result.get(0);
        assertEquals("SYS-123", dto1.getSystemCode());
        assertEquals(overview, dto1.getSolutionOverview());
        assertEquals(1, dto1.getBusinessCapabilities().size());
        assertEquals("bc-1", dto1.getBusinessCapabilities().get(0).getId());

        // Second review
        BusinessCapabilityDiagramDTO dto2 = result.get(1);
        assertEquals("SYS-456", dto2.getSystemCode());
        assertEquals(overview2, dto2.getSolutionOverview());
        assertEquals(2, dto2.getBusinessCapabilities().size());
        assertEquals("bc-2", dto2.getBusinessCapabilities().get(0).getId());
        assertEquals("bc-3", dto2.getBusinessCapabilities().get(1).getId());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getBusinessCapabilitySolutionReviews_ShouldPreserveOrderFromRepository() {
        // Arrange
        SolutionReview review1 = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-AAA")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        SolutionReview review2 = SolutionReview.newDraftBuilder()
                .id("rev-2")
                .systemCode("SYS-ZZZ")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        SolutionReview review3 = SolutionReview.newDraftBuilder()
                .id("rev-3")
                .systemCode("SYS-MMM")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .build();

        // Repository returns in specific order
        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(review2, review1, review3)); // ZZZ, AAA, MMM

        // Act
        List<BusinessCapabilityDiagramDTO> result = service.getBusinessCapabilitySolutionReviews();

        // Assert
        assertEquals(3, result.size());
        assertEquals("SYS-ZZZ", result.get(0).getSystemCode());
        assertEquals("SYS-AAA", result.get(1).getSystemCode());
        assertEquals("SYS-MMM", result.get(2).getSystemCode());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void getBusinessCapabilitySolutionReviews_ShouldUseCorrectFactoryMethod() {
        // Arrange
        SolutionReview activeReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.ACTIVE)
                .businessCapabilities(List.of())
                .build();

        when(solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE))
                .thenReturn(List.of(activeReview));

        // Act
        List<BusinessCapabilityDiagramDTO> result = service.getBusinessCapabilitySolutionReviews();

        // Assert
        assertEquals(1, result.size());
        BusinessCapabilityDiagramDTO dto = result.get(0);

        // Verify all three required fields are properly mapped
        assertEquals(activeReview.getSystemCode(), dto.getSystemCode());
        assertEquals(activeReview.getSolutionOverview(), dto.getSolutionOverview());
        assertEquals(activeReview.getBusinessCapabilities(), dto.getBusinessCapabilities());

        verify(solutionReviewRepository).findByDocumentState(DocumentState.ACTIVE);
    }

    @Test
    void createSolutionReview_ShouldThrowIfOverviewNull() {
        assertThrows(IllegalArgumentException.class, () -> service.createSolutionReview("SYS-123", null));
    }

    @Test
    void createSolutionReview_ShouldThrowIfSystemCodeNullOrEmpty() {
        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
                overview.getApplicationUsers(),
                overview.getConcerns());
        assertThrows(NullPointerException.class, () -> service.createSolutionReview(null, dto));
        assertThrows(IllegalArgumentException.class, () -> service.createSolutionReview("", dto));
    }

    @Test
    void createSolutionReview_ShouldThrowIfSystemCodeExistsWithDraftState() {
        // Arrange - existing DRAFT review
        review.setDocumentState(DocumentState.DRAFT);
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(List.of(review));
        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
                overview.getApplicationUsers(),
                overview.getConcerns());

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.createSolutionReview("SYS-123", dto));
        assertTrue(exception.getMessage().contains("Documents already exist in exclusive states"));
    }

    @Test
    void createSolutionReview_ShouldThrowIfSystemCodeExistsWithSubmittedState() {
        // Arrange - existing SUBMITTED review
        review.setDocumentState(DocumentState.SUBMITTED);
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(List.of(review));
        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
                overview.getApplicationUsers(),
                overview.getConcerns());

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.createSolutionReview("SYS-123", dto));
        assertTrue(exception.getMessage().contains("Documents already exist in exclusive states"));
    }

    @Test
    void createSolutionReview_ShouldAllowIfSystemCodeExistsWithActiveState() {
        // Arrange - existing ACTIVE review (should allow creation of new draft)
        review.setDocumentState(DocumentState.ACTIVE);
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(Collections.emptyList()); // Empty because ACTIVE is not in exclusive states
        when(solutionReviewRepository.insert(any(SolutionReview.class))).thenReturn(review);

        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
                overview.getApplicationUsers(),
                overview.getConcerns());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            SolutionReview result = service.createSolutionReview("SYS-123", dto);
            assertNotNull(result);
        });

        verify(solutionReviewRepository).insert(any(SolutionReview.class));
    }

    @Test
    void createSolutionReview_ShouldThrowIfSystemCodeExistsWithApprovedState() {
        // Arrange - existing APPROVED review
        review.setDocumentState(DocumentState.APPROVED);
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(List.of(review));
        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
                overview.getApplicationUsers(),
                overview.getConcerns());

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.createSolutionReview("SYS-123", dto));
        assertTrue(exception.getMessage().contains("Documents already exist in exclusive states"));
    }

    @Test
    void createSolutionReview_ShouldAllowIfSystemCodeExistsWithOutdatedState() {
        // Arrange - existing OUTDATED review (should allow creation of new draft)
        review.setDocumentState(DocumentState.OUTDATED);
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(Collections.emptyList()); // Empty because OUTDATED is not in exclusive
                                                      // states
        when(solutionReviewRepository.insert(any(SolutionReview.class))).thenReturn(review);

        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
                overview.getApplicationUsers(),
                overview.getConcerns());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            SolutionReview result = service.createSolutionReview("SYS-123", dto);
            assertNotNull(result);
        });

        verify(solutionReviewRepository).insert(any(SolutionReview.class));
    }

    @Test
    void createSolutionReview_ShouldThrowIfOverviewInvalid() {
        assertThrows(IllegalArgumentException.class, () -> service.createSolutionReview("SYS-123", null));
    }

    @Test
    void createSolutionReview_ShouldSaveAndInsert() {
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(Collections.emptyList()); // No existing exclusive state documents
        when(solutionReviewRepository.insert(any(SolutionReview.class))).thenReturn(review);
        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
                overview.getApplicationUsers(),
                overview.getConcerns());
        SolutionReview result = service.createSolutionReview("SYS-123", dto);

        assertNotNull(result);
        assertEquals("SYS-123", result.getSystemCode());
        verify(solutionReviewRepository).insert(any(SolutionReview.class));
    }

    @Test
    void createSolutionReviewFromExisting_ShouldThrowIfSystemCodeNullOrEmpty() {
        assertThrows(NotFoundException.class, () -> service.createSolutionReview(null));
        assertThrows(NotFoundException.class, () -> service.createSolutionReview(""));
    }

    @Test
    void createSolutionReviewFromExisting_ShouldThrowIfSystemCodeNotFound() {
        when(solutionReviewRepository.findFirstBySystemCodeAndDocumentStateIn(eq("NOT-EXIST"), anyList()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createSolutionReview("NOT-EXIST"));
    }

    @Test
    void createSolutionReviewFromExisting_ShouldCreateFromActiveReview() {
        // Mock active review exists
        review.setDocumentState(DocumentState.ACTIVE);
        when(solutionReviewRepository.findFirstBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(Optional.of(review));

        // Mock constraint validation passes for exclusive states (DRAFT, SUBMITTED,
        // APPROVED)
        // This should return empty because ACTIVE is not in exclusive states anymore
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn("SYS-123",
                List.copyOf(DocumentState.getExclusiveStates())))
                .thenReturn(Collections.emptyList());

        when(solutionReviewRepository.insert(any(SolutionReview.class)))
                .thenReturn(review);

        SolutionReview result = service.createSolutionReview("SYS-123");
        assertNotNull(result);
        assertEquals("SYS-123", result.getSystemCode());
        verify(solutionReviewRepository).insert(any(SolutionReview.class));
    }

    @Test
    void createSolutionReviewFromExisting_ShouldAllowDraftWhenActiveExists() {
        // This test verifies the bug fix: we can create a DRAFT when an ACTIVE exists
        // because they have separate constraints

        // Mock active review exists
        review.setDocumentState(DocumentState.ACTIVE);
        when(solutionReviewRepository.findFirstBySystemCodeAndDocumentStateIn("SYS-123",
                List.of(DocumentState.ACTIVE)))
                .thenReturn(Optional.of(review));

        // Mock that no exclusive state documents exist (DRAFT, SUBMITTED, APPROVED)
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn("SYS-123",
                List.copyOf(DocumentState.getExclusiveStates())))
                .thenReturn(Collections.emptyList());

        when(solutionReviewRepository.insert(any(SolutionReview.class)))
                .thenReturn(review);

        // This should not throw an exception
        assertDoesNotThrow(() -> {
            SolutionReview result = service.createSolutionReview("SYS-123");
            assertNotNull(result);
            assertEquals("SYS-123", result.getSystemCode());
        });

        verify(solutionReviewRepository).insert(any(SolutionReview.class));
    }

    @Test
    void updateSolutionReview_ShouldThrowIfDTONull() {
        assertThrows(IllegalArgumentException.class, () -> service.updateSolutionReview(null));
    }

    @Test
    void updateSolutionReview_FoundAndDraft() {
        SolutionReviewDTO dto = new SolutionReviewDTO();
        dto.setId("rev-1");
        dto.setDocumentState(DocumentState.DRAFT);

        when(solutionReviewRepository.findById("rev-1")).thenReturn(Optional.of(review));
        when(solutionReviewRepository.save(any())).thenReturn(review);

        SolutionReview updated = service.updateSolutionReview(dto);

        assertNotNull(updated);
        verify(solutionReviewRepository).save(any(SolutionReview.class));
    }

    @Test
    void updateSolutionReview_NotFound() {
        SolutionReviewDTO dto = new SolutionReviewDTO();
        dto.setId("not-exist");

        when(solutionReviewRepository.findById("not-exist")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateSolutionReview(dto));
    }

    @Test
    void updateSolutionReview_NotDraft_Throws() {
        review.setDocumentState(DocumentState.OUTDATED);

        SolutionReviewDTO dto = new SolutionReviewDTO();
        dto.setId("rev-1");

        when(solutionReviewRepository.findById("rev-1")).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class, () -> service.updateSolutionReview(dto));
    }

    @Test
    void updateSolutionReviewConcerns_ShouldUpdateConcernsForSubmittedReview() {
        // Arrange
        String reviewId = "review-123";
        SolutionReview existingReview = SolutionReview.newDraftBuilder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.SUBMITTED)
                .build();

        Concern newConcern = new Concern("concern-1", ConcernType.RISK, "New concern", "High impact",
                "Under review", ConcernStatus.UNKNOWN,
                LocalDateTime.of(2025, 10, 25, 14, 15, 46, 372370000));
        SolutionOverview modifiedOverview = SolutionOverview.fromExisting(overview)
                .concerns(List.of(newConcern))
                .build();

        SolutionReviewDTO modifiedDTO = SolutionReviewDTO.builder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(modifiedOverview)
                .build();

        when(solutionReviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(existingReview);

        // Act
        SolutionReview result = service.updateSolutionReviewConcerns(modifiedDTO);

        // Assert
        assertNotNull(result);
        verify(solutionReviewRepository).findById(reviewId);
        verify(solutionReviewRepository).save(any(SolutionReview.class));
    }

    @Test
    void updateSolutionReviewConcerns_ShouldThrowExceptionWhenReviewNotSubmitted() {
        // Arrange
        String reviewId = "review-123";
        SolutionReview draftReview = SolutionReview.newDraftBuilder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .build();

        SolutionReviewDTO modifiedDTO = SolutionReviewDTO.builder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .build();

        when(solutionReviewRepository.findById(reviewId)).thenReturn(Optional.of(draftReview));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.updateSolutionReviewConcerns(modifiedDTO));

        assertEquals("Only SUBMITTED reviews can have concerns updated", exception.getMessage());
        verify(solutionReviewRepository).findById(reviewId);
        verify(solutionReviewRepository, never()).save(any());
    }

    @Test
    void updateSolutionReviewConcerns_ShouldThrowExceptionWhenReviewNotFound() {
        // Arrange
        String reviewId = "non-existent-review";
        SolutionReviewDTO modifiedDTO = SolutionReviewDTO.builder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .build();

        when(solutionReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.updateSolutionReviewConcerns(modifiedDTO));

        assertEquals(reviewId, exception.getMessage());
        verify(solutionReviewRepository).findById(reviewId);
        verify(solutionReviewRepository, never()).save(any());
    }

    @Test
    void updateSolutionReviewConcerns_ShouldThrowExceptionWhenDTOIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSolutionReviewConcerns(null));

        assertEquals("Modified SolutionReview cannot be null", exception.getMessage());
        verify(solutionReviewRepository, never()).findById(any());
    }

    @Test
    void updateSolutionReviewConcerns_ShouldThrowExceptionWhenApprovedStateReview() {
        // Arrange
        String reviewId = "review-123";
        SolutionReview approvedReview = SolutionReview.newDraftBuilder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.APPROVED)
                .build();

        SolutionReviewDTO modifiedDTO = SolutionReviewDTO.builder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .build();

        when(solutionReviewRepository.findById(reviewId)).thenReturn(Optional.of(approvedReview));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.updateSolutionReviewConcerns(modifiedDTO));

        assertEquals("Only SUBMITTED reviews can have concerns updated", exception.getMessage());
        verify(solutionReviewRepository).findById(reviewId);
        verify(solutionReviewRepository, never()).save(any());
    }

    @Test
    void updateSolutionReviewConcerns_ShouldHandleNullSolutionOverview() {
        // Arrange
        String reviewId = "review-123";
        SolutionReview existingReview = SolutionReview.newDraftBuilder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.SUBMITTED)
                .build();

        SolutionReviewDTO modifiedDTO = SolutionReviewDTO.builder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(null) // No solution overview provided
                .build();

        when(solutionReviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(solutionReviewRepository.save(any(SolutionReview.class))).thenReturn(existingReview);

        // Act
        SolutionReview result = service.updateSolutionReviewConcerns(modifiedDTO);

        // Assert
        assertNotNull(result);
        verify(solutionReviewRepository).findById(reviewId);
        verify(solutionReviewRepository).save(any(SolutionReview.class));
        // Should not call solutionOverviewRepository.save when overview is null
    }

    @Test
    void updateSolutionReviewConcerns_ShouldUpdateLastModifiedAt() {
        // Arrange
        String reviewId = "review-123";
        LocalDateTime originalTime = LocalDateTime.now().minusHours(1);
        SolutionReview existingReview = SolutionReview.newDraftBuilder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.SUBMITTED)
                .lastModifiedAt(originalTime)
                .build();

        SolutionReviewDTO modifiedDTO = SolutionReviewDTO.builder()
                .id(reviewId)
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .build();

        when(solutionReviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(solutionReviewRepository.save(any(SolutionReview.class))).thenAnswer(invocation -> {
            SolutionReview savedReview = invocation.getArgument(0);
            // Verify that lastModifiedAt was updated
            assertTrue(savedReview.getLastModifiedAt().isAfter(originalTime));
            return savedReview;
        });

        // Act
        SolutionReview result = service.updateSolutionReviewConcerns(modifiedDTO);

        // Assert
        assertNotNull(result);
        verify(solutionReviewRepository).save(any(SolutionReview.class));
    }

    @Test
    void deleteSolutionReview_FoundAndDraft() {
        when(solutionReviewRepository.findById("rev-1")).thenReturn(Optional.of(review));

        service.deleteSolutionReview("rev-1");

        verify(solutionReviewRepository).deleteById("rev-1");
    }

    @Test
    void deleteSolutionReview_NotFound() {
        when(solutionReviewRepository.findById("x")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteSolutionReview("x"));
    }

    @Test
    void deleteSolutionReview_NotDraft_Throws() {
        review.setDocumentState(DocumentState.OUTDATED);
        when(solutionReviewRepository.findById("rev-1")).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class, () -> service.deleteSolutionReview("rev-1"));
    }

    @Test
    void validateExclusiveStateConstraint_ShouldPassWhenNoExistingDocuments() {
        // Arrange
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(Collections.emptyList());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> service.validateExclusiveStateConstraint("SYS-123"));
    }

    @Test
    void validateExclusiveStateConstraint_ShouldThrowWhenExistingDocumentFound() {
        // Arrange
        review.setDocumentState(DocumentState.DRAFT);
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(List.of(review));

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.validateExclusiveStateConstraint("SYS-123"));
        assertTrue(exception.getMessage().contains("Documents already exist in exclusive states"));
        assertTrue(exception.getMessage()
                .contains("Only one document can be in DRAFT, SUBMITTED, or APPROVED state"));
    }

    @Test
    void validateExclusiveStateConstraint_ShouldPassWhenExcludingSpecificDocument() {
        // Arrange
        review.setDocumentState(DocumentState.DRAFT);
        review.setId("rev-1");
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(List.of(review));

        // Act & Assert - Should not throw exception when excluding the same document
        assertDoesNotThrow(() -> service.validateExclusiveStateConstraint("SYS-123", "rev-1"));
    }

    @Test
    void validateExclusiveStateConstraint_ShouldThrowWhenExcludingDifferentDocument() {
        // Arrange
        review.setDocumentState(DocumentState.DRAFT);
        review.setId("rev-1");
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(eq("SYS-123"), anyList()))
                .thenReturn(List.of(review));

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.validateExclusiveStateConstraint("SYS-123", "different-id"));
        assertTrue(exception.getMessage().contains("Documents already exist in exclusive states"));
    }

    @Test
    void validateActiveStateConstraint_ShouldPassWhenNoActiveDocumentExists() {
        // Arrange
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn("SYS-123",
                List.of(DocumentState.ACTIVE)))
                .thenReturn(Collections.emptyList());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> service.validateActiveStateConstraint("SYS-123"));
    }

    @Test
    void validateActiveStateConstraint_ShouldThrowWhenActiveDocumentExists() {
        // Arrange
        review.setDocumentState(DocumentState.ACTIVE);
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn("SYS-123",
                List.of(DocumentState.ACTIVE)))
                .thenReturn(List.of(review));

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.validateActiveStateConstraint("SYS-123"));
        assertTrue(exception.getMessage().contains("An ACTIVE document already exists"));
        assertTrue(exception.getMessage().contains("Only one document can be in ACTIVE state at a time"));
    }

    @Test
    void validateActiveStateConstraint_ShouldPassWhenExcludingSpecificDocument() {
        // Arrange
        review.setDocumentState(DocumentState.ACTIVE);
        review.setId("rev-1");
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn("SYS-123",
                List.of(DocumentState.ACTIVE)))
                .thenReturn(List.of(review));

        // Act & Assert - Should not throw exception when excluding the same document
        assertDoesNotThrow(() -> service.validateActiveStateConstraint("SYS-123", "rev-1"));
    }

    @Test
    void validateActiveStateConstraint_ShouldThrowWhenExcludingDifferentDocument() {
        // Arrange
        review.setDocumentState(DocumentState.ACTIVE);
        review.setId("rev-1");
        when(solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn("SYS-123",
                List.of(DocumentState.ACTIVE)))
                .thenReturn(List.of(review));

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.validateActiveStateConstraint("SYS-123", "different-id"));
        assertTrue(exception.getMessage().contains("An ACTIVE document already exists"));
    }

}
