package com.project.core_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.project.core_service.dto.NewSolutionOverviewRequestDTO;
import com.project.core_service.dto.SolutionReviewDTO;
import com.project.core_service.exceptions.IllegalOperationException;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.solution_overview.*;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.repositories.*;
import com.project.core_service.services.SolutionReviewService;
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
    private SolutionOverviewRepository solutionOverviewRepository;
    @Mock
    private ConcernRepository concernRepository;
    @Mock
    private ToolRepository toolRepository;
    @Mock
    private BusinessCapabilityRepository businessCapabilityRepository;
    @Mock
    private SystemComponentRepository systemComponentRepository;
    @Mock
    private IntegrationFlowRepository integrationFlowRepository;
    @Mock
    private DataAssetRepository dataAssetRepository;
    @Mock
    private TechnologyComponentRepository technologyComponentRepository;
    @Mock
    private EnterpriseToolRepository enterpriseToolRepository;
    @Mock
    private ProcessCompliantRepository processCompliantRepository;

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
                ConcernStatus.UNKNOWN);
        return List.of(dummyConcern);
    }

    @BeforeEach
    void setup() {
        overview = new SolutionOverview(
                "id-001",
                dummySolutionDetails(),
                "ReviewerName",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
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

        Page<SolutionReview> result = service.getSolutionReviews(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getSolutionReviewsBySystemCode() {
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(review));

        List<SolutionReview> result = service.getSolutionReviewsBySystemCode("SYS-123");

        assertEquals(1, result.size());
        assertEquals("SYS-123", result.get(0).getSystemCode());
    }

    @Test
    void getAllSolutionReviews() {
        when(solutionReviewRepository.findAll(any(Sort.class))).thenReturn(List.of(review));

        List<SolutionReview> result = service.getAllSolutionReviews();

        assertEquals(1, result.size());
    }

    @Test
    void getPaginatedSystemView_ShouldReturnApprovedReviews() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123", "SYS-456");
        SolutionReview approvedReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.CURRENT)
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findApprovedBySystemCode("SYS-123")).thenReturn(Optional.of(approvedReview));
        when(solutionReviewRepository.findApprovedBySystemCode("SYS-456")).thenReturn(Optional.empty());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-456"), any(Sort.class)))
                .thenReturn(List.of());

        // Act
        Page<SolutionReview> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(1, result.getTotalElements()); // Only one system has reviews
        assertEquals(1, result.getContent().size());
        assertEquals("SYS-123", result.getContent().get(0).getSystemCode());
        assertEquals(DocumentState.CURRENT, result.getContent().get(0).getDocumentState());
    }

    @Test
    void getPaginatedSystemView_ShouldReturnCurrentWhenDraftExistsWithLaterCreationTime() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123");

        SolutionReview currentReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.CURRENT)
                .lastModifiedAt(LocalDateTime.now().minusDays(1))
                .build();

        SolutionReview draftReview = SolutionReview.newDraftBuilder()
                .id("rev-2")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .lastModifiedAt(LocalDateTime.now()) // More recent but should not be returned
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findApprovedBySystemCode("SYS-123")).thenReturn(Optional.of(currentReview));

        // Act
        Page<SolutionReview> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("SYS-123", result.getContent().get(0).getSystemCode());
        assertEquals(DocumentState.CURRENT, result.getContent().get(0).getDocumentState());
        assertEquals("rev-1", result.getContent().get(0).getId());
    }

    @Test
    void getPaginatedSystemView_ShouldReturnDraftForOneSystemAndCurrentForAnother() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123", "SYS-456");

        SolutionReview currentReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.CURRENT)
                .lastModifiedAt(LocalDateTime.now().minusDays(1))
                .build();

        SolutionReview draftReviewForSystem456 = SolutionReview.newDraftBuilder()
                .id("rev-3")
                .systemCode("SYS-456")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .lastModifiedAt(LocalDateTime.now())
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findApprovedBySystemCode("SYS-123")).thenReturn(Optional.of(currentReview));
        when(solutionReviewRepository.findApprovedBySystemCode("SYS-456")).thenReturn(Optional.empty());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-456"), any(Sort.class)))
                .thenReturn(List.of(draftReviewForSystem456));

        // Act
        Page<SolutionReview> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        // Find the reviews by system code since order might vary
        SolutionReview sys123Review = result.getContent().stream()
                .filter(r -> "SYS-123".equals(r.getSystemCode()))
                .findFirst().orElse(null);
        SolutionReview sys456Review = result.getContent().stream()
                .filter(r -> "SYS-456".equals(r.getSystemCode()))
                .findFirst().orElse(null);

        assertNotNull(sys123Review);
        assertEquals(DocumentState.CURRENT, sys123Review.getDocumentState());

        assertNotNull(sys456Review);
        assertEquals(DocumentState.DRAFT, sys456Review.getDocumentState());
    }

    @Test
    void getPaginatedSystemView_ShouldReturnLatestReviewIfNoApprovedExists() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123");

        SolutionReview olderReview = SolutionReview.newDraftBuilder()
                .id("rev-1")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.SUBMITTED)
                .lastModifiedAt(LocalDateTime.now().minusDays(1))
                .build();

        SolutionReview latestReview = SolutionReview.newDraftBuilder()
                .id("rev-2")
                .systemCode("SYS-123")
                .solutionOverview(overview)
                .documentState(DocumentState.DRAFT)
                .lastModifiedAt(LocalDateTime.now())
                .build();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findApprovedBySystemCode("SYS-123")).thenReturn(Optional.empty());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(latestReview, olderReview)); // Sorted by lastModifiedAt DESC

        // Act
        Page<SolutionReview> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("SYS-123", result.getContent().get(0).getSystemCode());
        assertEquals(DocumentState.DRAFT, result.getContent().get(0).getDocumentState());
        assertEquals("rev-2", result.getContent().get(0).getId()); // Latest review
    }

    @Test
    void getPaginatedSystemView_ShouldHandleEmptySystemCodes() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of();

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);

        // Act
        Page<SolutionReview> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getPaginatedSystemView_ShouldHandlePagination() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 2); // Second page, 2 items per page
        List<String> systemCodes = List.of("SYS-001", "SYS-002", "SYS-003", "SYS-004", "SYS-005");

        // Create 5 reviews for different systems
        List<SolutionReview> reviews = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            SolutionReview review = SolutionReview.newDraftBuilder()
                    .id("rev-" + i)
                    .systemCode("SYS-00" + i)
                    .solutionOverview(overview)
                    .documentState(DocumentState.CURRENT)
                    .build();
            reviews.add(review);
            when(solutionReviewRepository.findApprovedBySystemCode("SYS-00" + i))
                    .thenReturn(Optional.of(review));
        }

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);

        // Act
        Page<SolutionReview> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertEquals(5, result.getTotalElements()); // Total across all pages
        assertEquals(2, result.getContent().size()); // Current page size
        assertEquals(1, result.getNumber()); // Current page number
        assertEquals(3, result.getTotalPages()); // Total pages (5 items, 2 per page = 3 pages)

        // Verify we got the correct items for page 1 (0-indexed)
        assertEquals("SYS-003", result.getContent().get(0).getSystemCode());
        assertEquals("SYS-004", result.getContent().get(1).getSystemCode());
    }

    @Test
    void getPaginatedSystemView_ShouldHandleNoReviewsForSystem() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<String> systemCodes = List.of("SYS-123");

        when(solutionReviewRepository.findAllDistinctSystemCodes()).thenReturn(systemCodes);
        when(solutionReviewRepository.findApprovedBySystemCode("SYS-123")).thenReturn(Optional.empty());
        when(solutionReviewRepository.findBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of());

        // Act
        Page<SolutionReview> result = service.getPaginatedSystemView(pageable);

        // Assert
        assertTrue(result.isEmpty()); // Systems with no reviews are filtered out
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
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
        assertThrows(NullPointerException.class, () -> service.createSolutionReview("", dto));
    }

    @Test
    void createSolutionReview_ShouldThrowIfSystemCodeExistsWithDraftState() {
        // Arrange - existing DRAFT review
        review.setDocumentState(DocumentState.DRAFT);
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
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
        assertTrue(exception.getMessage().contains("A DRAFT or SUBMITTED review already exists for system"));
    }

    @Test
    void createSolutionReview_ShouldThrowIfSystemCodeExistsWithSubmittedState() {
        // Arrange - existing SUBMITTED review
        review.setDocumentState(DocumentState.SUBMITTED);
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
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
        assertTrue(exception.getMessage().contains("A DRAFT or SUBMITTED review already exists for system"));
    }

    @Test
    void createSolutionReview_ShouldAllowCreationWhenCurrentStateExists() {
        // Arrange - existing CURRENT review
        review.setDocumentState(DocumentState.CURRENT);
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(review));
        when(solutionOverviewRepository.save(any())).thenReturn(overview);
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
    void createSolutionReview_ShouldAllowIfSystemCodeExistsWithOutdatedState() {
        // Arrange - existing OUTDATED review (should allow creation of new draft)
        review.setDocumentState(DocumentState.OUTDATED);
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(review));
        when(solutionOverviewRepository.save(any())).thenReturn(overview);
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
    void createSolutionReview_ShouldThrowIfConcernInvalid() {
        List<Concern> invalidConcerns = List
                .of(new Concern(null, ConcernType.RISK, "desc", "impact", "disposition", ConcernStatus.UNKNOWN));
        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
                overview.getApplicationUsers(),
                invalidConcerns);
        assertThrows(NullPointerException.class, () -> service.createSolutionReview("SYS-123", dto));
    }

    @Test
    void createSolutionReview_ShouldSaveAndInsert() {
        when(solutionOverviewRepository.save(any())).thenReturn(overview);
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
        when(solutionReviewRepository.findAllBySystemCode(eq("NOT-EXIST"), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> service.createSolutionReview("NOT-EXIST"));
    }

    @Test
    void createSolutionReviewFromExisting_ShouldSucceedWhenCurrentStateExists() {
        review.setDocumentState(DocumentState.CURRENT);
        when(solutionOverviewRepository.save(any())).thenReturn(overview);
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(review));
        when(solutionReviewRepository.insert(any(SolutionReview.class)))
                .thenReturn(review);

        SolutionReview review1 = service.createSolutionReview("SYS-123");
        assertNotNull(review1);
        assertEquals("SYS-123", review1.getSystemCode());
        verify(solutionReviewRepository).insert(any(SolutionReview.class));
    }

    @Test
    void createSolutionReviewFromExisting_ShouldThrowIfDraftExists() {
        // Test blocking behavior for DRAFT state
        review.setDocumentState(DocumentState.DRAFT);
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(review));

        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.createSolutionReview("SYS-123"));
        assertTrue(exception.getMessage().contains("A DRAFT or SUBMITTED review already exists"));
    }

    @Test
    void createSolutionReviewFromExisting_ShouldThrowIfSubmittedExists() {
        // Test blocking behavior for SUBMITTED state
        review.setDocumentState(DocumentState.SUBMITTED);
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(review));

        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.createSolutionReview("SYS-123"));
        assertTrue(exception.getMessage().contains("A DRAFT or SUBMITTED review already exists"));
    }

    @Test
    void createSolutionReviewFromExisting_ShouldSucceedWhenOutdatedExists() {
        // Test allowing creation when only OUTDATED exists
        review.setDocumentState(DocumentState.OUTDATED);
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(review));
        when(solutionOverviewRepository.save(any())).thenReturn(overview);
        when(solutionReviewRepository.insert(any(SolutionReview.class))).thenReturn(review);

        assertDoesNotThrow(() -> {
            SolutionReview result = service.createSolutionReview("SYS-123");
            assertNotNull(result);
        });
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
    void deleteSolutionReview_FoundAndDraft() {
        when(solutionReviewRepository.findById("rev-1")).thenReturn(Optional.of(review));

        service.deleteSolutionReview("rev-1");

        verify(solutionReviewRepository).deleteById("rev-1");
        verify(solutionOverviewRepository).deleteById("id-001");
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
}
