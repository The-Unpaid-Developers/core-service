package com.project.core_service.service;

import static org.junit.jupiter.api.Assertions.*;
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
    void createSolutionReview_ShouldThrowIfOverviewNull() {
        assertThrows(IllegalArgumentException.class, () -> service.createSolutionReview("SYS-123", null));
    }

    @Test
    void createSolutionReview_ShouldThrowIfSystemCodeNullOrEmpty() {
        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
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
                overview.getConcerns());

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.createSolutionReview("SYS-123", dto));
        assertTrue(exception.getMessage().contains("A CURRENT or SUBMITTED or DRAFT review already exists"));
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
                overview.getConcerns());

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.createSolutionReview("SYS-123", dto));
        assertTrue(exception.getMessage().contains("A CURRENT or SUBMITTED or DRAFT review already exists"));
    }

    @Test
    void createSolutionReview_ShouldThrowIfSystemCodeExistsWithCurrentState() {
        // Arrange - existing CURRENT review
        review.setDocumentState(DocumentState.CURRENT);
        when(solutionReviewRepository.findAllBySystemCode(eq("SYS-123"), any(Sort.class)))
                .thenReturn(List.of(review));
        NewSolutionOverviewRequestDTO dto = new NewSolutionOverviewRequestDTO(overview.getSolutionDetails(),
                overview.getBusinessUnit(),
                overview.getBusinessDriver(),
                overview.getValueOutcome(),
                overview.getConcerns());

        // Act & Assert
        IllegalOperationException exception = assertThrows(IllegalOperationException.class,
                () -> service.createSolutionReview("SYS-123", dto));
        assertTrue(exception.getMessage().contains("A CURRENT or SUBMITTED or DRAFT review already exists"));
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
    void createSolutionReviewFromExisting_ShouldThrowIfSystemCodeExists() {
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
