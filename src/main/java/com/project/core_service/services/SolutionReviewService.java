package com.project.core_service.services;

import com.project.core_service.dto.SystemDependencyDTO;
import com.project.core_service.dto.BusinessCapabilityDTO;
import com.project.core_service.dto.NewSolutionOverviewRequestDTO;
import com.project.core_service.dto.SolutionReviewDTO;
import com.project.core_service.exceptions.IllegalOperationException;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.enterprise_tools.EnterpriseTool;
import com.project.core_service.models.enterprise_tools.Tool;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service layer for managing {@link SolutionReview} entities.
 *
 * <p>
 * This service provides CRUD operations for solution reviews, as well as
 * support for pagination and partial updates. It acts as the business logic
 * layer between the controller and repository.
 * </p>
 */
@Service
public class SolutionReviewService {
    private final SolutionReviewRepository solutionReviewRepository;

    private final SolutionOverviewRepository solutionOverviewRepository;

    private final ConcernRepository concernRepository;

    private final ToolRepository toolRepository;

    private final BusinessCapabilityRepository businessCapabilityRepository;

    private final SystemComponentRepository systemComponentRepository;

    private final IntegrationFlowRepository integrationFlowRepository;

    private final DataAssetRepository dataAssetRepository;

    private final TechnologyComponentRepository technologyComponentRepository;

    private final EnterpriseToolRepository enterpriseToolRepository;

    private final ProcessCompliantRepository processCompliantRepository;

    @Autowired
    public SolutionReviewService(
            SolutionReviewRepository solutionReviewRepository,
            SolutionOverviewRepository solutionOverviewRepository,
            ConcernRepository concernRepository,
            ToolRepository toolRepository,
            BusinessCapabilityRepository businessCapabilityRepository,
            SystemComponentRepository systemComponentRepository,
            IntegrationFlowRepository integrationFlowRepository,
            DataAssetRepository dataAssetRepository,
            TechnologyComponentRepository technologyComponentRepository,
            EnterpriseToolRepository enterpriseToolRepository,
            ProcessCompliantRepository processCompliantRepository) {
        this.solutionReviewRepository = solutionReviewRepository;
        this.solutionOverviewRepository = solutionOverviewRepository;
        this.concernRepository = concernRepository;
        this.toolRepository = toolRepository;
        this.businessCapabilityRepository = businessCapabilityRepository;
        this.systemComponentRepository = systemComponentRepository;
        this.integrationFlowRepository = integrationFlowRepository;
        this.dataAssetRepository = dataAssetRepository;
        this.technologyComponentRepository = technologyComponentRepository;
        this.enterpriseToolRepository = enterpriseToolRepository;
        this.processCompliantRepository = processCompliantRepository;
    }

    /**
     * Retrieves a {@link SolutionReview} by its ID.
     *
     * @param id the identifier of the solution review
     * @return an {@link Optional} containing the solution review if found,
     *         or empty if no review exists for the given ID
     */
    public Optional<SolutionReview> getSolutionReviewById(String id) {
        return solutionReviewRepository.findById(id);
    }

    /**
     * Retrieves all {@link SolutionReview} entries.
     *
     * @return a {@link List} of all solution reviews
     */
    public List<SolutionReview> getAllSolutionReviews() {
        return solutionReviewRepository.findAll(Sort.by(Sort.Direction.DESC, "lastModifiedAt"));
    }

    /**
     * Retrieves all {@link SolutionReview} entries with pagination.
     *
     * @param pageable the pagination information
     * @return a {@link Page} of solution reviews
     */
    public Page<SolutionReview> getSolutionReviews(Pageable pageable) {
        return solutionReviewRepository.findAll(pageable);
    }

    public Optional<SolutionReview> getActiveSolutionReviewBySystemCode(String systemCode) {
        return solutionReviewRepository.findFirstBySystemCodeAndDocumentStateIn(systemCode,
                List.of(DocumentState.ACTIVE));
    }

    /**
     * Retrieves {@link SolutionReview} entries filtered by system code.
     *
     * @param systemCode the system code used for filtering
     * @return a {@link List} of solution reviews for the given system code
     */
    public List<SolutionReview> getSolutionReviewsBySystemCode(String systemCode) {
        return solutionReviewRepository.findAllBySystemCode(systemCode);
    }

    /**
     * Retrieves a paginated view of solution reviews grouped by system.
     *
     * <p>
     * For each system, if an approved solution review exists, it is returned.
     * Otherwise, the latest solution review for the system is returned.
     * </p>
     *
     * @param pageable the pagination information
     * @return a {@link Page} of solution reviews, one per system
     */
    public Page<SolutionReview> getPaginatedSystemView(Pageable pageable) {
        List<String> allSystemCodes = solutionReviewRepository.findAllDistinctSystemCodes();

        // Get ALL representative reviews first
        List<SolutionReview> allRepresentativeReviews = allSystemCodes.stream()
                .map(this::getRepresentativeReviewForSystem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        // Apply pagination AFTER filtering
        int total = allRepresentativeReviews.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        if (start >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        List<SolutionReview> paginatedList = allRepresentativeReviews.subList(start, end);
        return new PageImpl<>(paginatedList, pageable, total);
    }

    private Optional<SolutionReview> getRepresentativeReviewForSystem(String systemCode) {
        Optional<SolutionReview> activeReview = solutionReviewRepository.findActiveBySystemCode(systemCode);

        if (activeReview.isPresent()) {
            return activeReview;
        }

        List<SolutionReview> reviews = solutionReviewRepository.findBySystemCode(
                systemCode, Sort.by(Sort.Direction.DESC, "lastModifiedAt"));

        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.get(0));
    }

    /**
     * Retrieves all {@link SolutionReview} entries with a specific document state,
     * with pagination.
     *
     * @param documentState the document state used for filtering
     * @param pageable      the pagination information
     * @return a {@link Page} of solution reviews with the specified document state
     */
    public Page<SolutionReview> getSolutionReviewsByDocumentState(String documentStateStr, Pageable pageable) {
        if (documentStateStr == null || documentStateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid document state: " + documentStateStr +
                    ". Valid values: " + Arrays.toString(DocumentState.values()));
        }
        DocumentState documentState;
        try {
            documentState = DocumentState.valueOf(documentStateStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid document state: " + documentStateStr +
                    ". Valid values: " + Arrays.toString(DocumentState.values()));
        }

        return solutionReviewRepository.findByDocumentState(documentState, pageable);
    }

    /**
     * Retrieves all {@link SolutionReview} entries with ACTIVE document state.
     * Returns only the essential fields: systemCode, solutionOverview, and
     * integrationFlows.
     *
     * @return a {@link List} of active solution reviews with limited fields
     */
    public List<SystemDependencyDTO> getSystemDependencySolutionReviews() {
        List<SolutionReview> activeSolutionReviews = solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE);
        return activeSolutionReviews.stream()
                .map(SystemDependencyDTO::fromSolutionReview)
                .toList();
    }

    /**
     * Retrieves all {@link SolutionReview} entries with ACTIVE document state.
     * Returns only the essential fields: systemCode, solutionOverview, and
     * businessCapabilities.
     *
     * @return a {@link List} of active solution reviews with limited fields
     */
    public List<BusinessCapabilityDTO> getBusinessCapabilitySolutionReviews() {
        List<SolutionReview> activeSolutionReviews = solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE);
        return activeSolutionReviews.stream()
                .map(BusinessCapabilityDTO::fromSolutionReview)
                .toList();
    }

    /**
     * Creates a new draft {@link SolutionReview}.
     *
     * @param systemCode       the system code associated with the review
     * @param solutionOverview the solution overview details
     * @return the newly created solution review
     */
    public SolutionReview createSolutionReview(String systemCode, NewSolutionOverviewRequestDTO solutionOverview) {
        validateExclusiveStateConstraint(systemCode);

        if (solutionOverview == null) {
            throw new IllegalArgumentException("SolutionOverview cannot be null");
        }

        SolutionOverview overview = solutionOverview.toNewDraftEntity();
        SolutionOverview savedOverview = saveSolutionOverview(overview);
        SolutionReview solutionReview = SolutionReview.newDraftBuilder()
                .id(solutionOverview.getSolutionDetails().getSolutionName().replace(" ", "-") + "-1")
                .systemCode(systemCode)
                .solutionOverview(savedOverview)
                .build();
        return solutionReviewRepository.insert(solutionReview);
    }

    /**
     * Creates a new draft {@link SolutionReview} from existing ACTIVE solution
     * review
     *
     * @param systemCode the system code associated with the review
     * @return the newly created solution review
     * @throws NotFoundException if no ACTIVE solution review exists for the system
     */
    public SolutionReview createSolutionReview(String systemCode) {
        // step 1: query for active solution review by system code (should only have 1,
        // optionally nothing -> throw error)
        Optional<SolutionReview> activeSolutionReviewOpt = getActiveSolutionReviewBySystemCode(systemCode);
        if (activeSolutionReviewOpt.isEmpty()) {
            throw new NotFoundException("No ACTIVE solution review found for system " + systemCode +
                    ". Cannot create new draft without an active reference.");
        }

        SolutionReview activeSolutionReview = activeSolutionReviewOpt.get();

        // Validate exclusive state constraint before creating new draft
        validateExclusiveStateConstraint(systemCode);

        // step 2: copy from existing ACTIVE solution review
        SolutionOverview savedOverview = saveSolutionOverview(
                SolutionOverview
                        .fromExisting(activeSolutionReview.getSolutionOverview())
                        .build());
        SolutionReview solutionReview = SolutionReview.fromExisting(activeSolutionReview, null)
                .solutionOverview(savedOverview).build();

        saveIfNotEmpty(solutionReview.getBusinessCapabilities(), businessCapabilityRepository,
                solutionReview::setBusinessCapabilities);
        saveIfNotEmpty(solutionReview.getSystemComponents(), systemComponentRepository,
                solutionReview::setSystemComponents);
        saveIfNotEmpty(solutionReview.getIntegrationFlows(), integrationFlowRepository,
                solutionReview::setIntegrationFlows);
        saveIfNotEmpty(solutionReview.getDataAssets(), dataAssetRepository, solutionReview::setDataAssets);
        saveIfNotEmpty(solutionReview.getTechnologyComponents(), technologyComponentRepository,
                solutionReview::setTechnologyComponents);
        saveEnterpriseTools(solutionReview, solutionReview.getEnterpriseTools());
        saveIfNotEmpty(solutionReview.getProcessCompliances(), processCompliantRepository,
                solutionReview::setProcessCompliances);
        return solutionReviewRepository.insert(solutionReview);
    }

    /**
     * Updates an existing {@link SolutionReview} with partial update logic.
     *
     * <p>
     * Only non-null or non-empty fields from the provided DTO are applied
     * to the existing entity. If the entity does not exist, a
     * {@link NotFoundException} is thrown.
     * </p>
     *
     * @param modifiedSolutionReview the DTO containing modified fields
     * @return the updated solution review
     * @throws NotFoundException if no solution review exists with the given ID
     */
    public SolutionReview updateSolutionReview(SolutionReviewDTO modifiedSolutionReview) {
        if (modifiedSolutionReview == null) {
            throw new IllegalArgumentException("Modified SolutionReview cannot be null");
        }
        // Check exists throw not found if not exists
        SolutionReview solutionReview = solutionReviewRepository.findById(modifiedSolutionReview.getId())
                .orElseThrow(() -> new NotFoundException(modifiedSolutionReview.getId()));

        if (solutionReview.getDocumentState() != DocumentState.DRAFT) {
            throw new IllegalStateException("Only DRAFT reviews can be modified");
        }

        // Update non-null / non-empty fields only (partial)
        solutionReview.setDocumentState(modifiedSolutionReview.getDocumentState());

        if (modifiedSolutionReview.getSolutionOverview() != null) {
            SolutionOverview overview = saveSolutionOverview(modifiedSolutionReview.getSolutionOverview());
            solutionReview.setSolutionOverview(overview);
        }

        saveIfNotEmpty(modifiedSolutionReview.getBusinessCapabilities(), businessCapabilityRepository,
                solutionReview::setBusinessCapabilities);
        saveIfNotEmpty(modifiedSolutionReview.getSystemComponents(), systemComponentRepository,
                solutionReview::setSystemComponents);
        saveIfNotEmpty(modifiedSolutionReview.getIntegrationFlows(), integrationFlowRepository,
                solutionReview::setIntegrationFlows);
        saveIfNotEmpty(modifiedSolutionReview.getDataAssets(), dataAssetRepository, solutionReview::setDataAssets);
        saveIfNotEmpty(modifiedSolutionReview.getTechnologyComponents(), technologyComponentRepository,
                solutionReview::setTechnologyComponents);
        saveEnterpriseTools(solutionReview, modifiedSolutionReview.getEnterpriseTools());
        saveIfNotEmpty(modifiedSolutionReview.getProcessCompliances(), processCompliantRepository,
                solutionReview::setProcessCompliances);

        solutionReview.setLastModifiedAt(LocalDateTime.now());

        return solutionReviewRepository.save(solutionReview);
    }

    /**
     * Updates concerns in an existing {@link SolutionReview} that is in SUBMITTED
     * state.
     *
     * The solution review must be in SUBMITTED state for this operation to be
     * allowed.
     *
     * @param modifiedSolutionReview the DTO containing the updated concerns
     * @return the updated solution review
     * @throws NotFoundException     if no solution review exists with the given ID
     * @throws IllegalStateException if the solution review is not in SUBMITTED
     *                               state
     */
    public SolutionReview updateSolutionReviewConcerns(SolutionReviewDTO modifiedSolutionReview) {
        if (modifiedSolutionReview == null) {
            throw new IllegalArgumentException("Modified SolutionReview cannot be null");
        }

        // Check exists throw not found if not exists
        SolutionReview solutionReview = solutionReviewRepository.findById(modifiedSolutionReview.getId())
                .orElseThrow(() -> new NotFoundException(modifiedSolutionReview.getId()));

        // Validate that the solution review is in SUBMITTED state
        if (solutionReview.getDocumentState() != DocumentState.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED reviews can have concerns updated");
        }

        // Update solution overview (including concerns)
        if (modifiedSolutionReview.getSolutionOverview() != null) {
            SolutionOverview overview = saveSolutionOverview(modifiedSolutionReview.getSolutionOverview());
            solutionReview.setSolutionOverview(overview);
        }

        solutionReview.setLastModifiedAt(LocalDateTime.now());

        return solutionReviewRepository.save(solutionReview);
    }

    /**
     * Deletes a {@link SolutionReview} by its ID.
     *
     * @param id the identifier of the solution review to delete
     */
    public void deleteSolutionReview(String id) {
        // Check exists throw not found if not exists
        SolutionReview solutionReview = solutionReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));

        // Check state is DRAFT else throw illegal state
        if (solutionReview.getDocumentState() != DocumentState.DRAFT) {
            throw new IllegalStateException("Only DRAFT reviews can be deleted");
        }

        // Cascade delete related entities
        businessCapabilityRepository.deleteAll(solutionReview.getBusinessCapabilities());
        systemComponentRepository.deleteAll(solutionReview.getSystemComponents());
        integrationFlowRepository.deleteAll(solutionReview.getIntegrationFlows());
        dataAssetRepository.deleteAll(solutionReview.getDataAssets());
        technologyComponentRepository.deleteAll(solutionReview.getTechnologyComponents());
        toolRepository.deleteAll(
                solutionReview.getEnterpriseTools().stream().map(EnterpriseTool::getTool).toList());
        enterpriseToolRepository.deleteAll(solutionReview.getEnterpriseTools());
        processCompliantRepository.deleteAll(solutionReview.getProcessCompliances());
        if (solutionReview.getSolutionOverview().getConcerns() != null) {
            concernRepository.deleteAll(solutionReview.getSolutionOverview().getConcerns());
        }
        solutionOverviewRepository.deleteById(solutionReview.getSolutionOverview().getId());
        solutionReviewRepository.deleteById(id);
    }

    private SolutionOverview saveSolutionOverview(SolutionOverview solutionOverview) {
        if (solutionOverview.getConcerns() != null) {
            saveIfNotEmpty(
                    solutionOverview.getConcerns(),
                    concernRepository,
                    solutionOverview::setConcerns);
        }
        return solutionOverviewRepository.save(solutionOverview);
    }

    private void saveEnterpriseTools(SolutionReview solutionReview, List<EnterpriseTool> enterpriseTools) {
        if (enterpriseTools != null && !enterpriseTools.isEmpty()) {
            enterpriseTools.stream()
                    .forEach(enterpriseTool -> {
                        Tool tool = toolRepository.save(enterpriseTool.getTool());
                        enterpriseTool.setTool(tool);
                    });
            List<EnterpriseTool> savedEnterpriseTools = enterpriseToolRepository.saveAll(enterpriseTools);

            solutionReview.setEnterpriseTools(savedEnterpriseTools);

        }
    }

    private <T> void saveIfNotEmpty(List<T> entities, MongoRepository<T, String> repository, Consumer<List<T>> setter) {
        if (entities != null && !entities.isEmpty()) {
            List<T> saved = repository.saveAll(entities);
            setter.accept(saved);
        }
    }

    // ====== CONSTRAINT VALIDATION METHODS ======

    /**
     * Validates that only one document exists in exclusive states (DRAFT,
     * SUBMITTED, APPROVED) for the given system code.
     * 
     * @param systemCode the system code to validate
     * @param excludeId  optional document ID to exclude from the check (for
     *                   updates)
     * @throws IllegalOperationException if constraint is violated
     */
    public void validateExclusiveStateConstraint(String systemCode, String excludeId) {
        List<DocumentState> exclusiveStates = List.copyOf(DocumentState.getExclusiveStates());
        List<SolutionReview> existingDocs = solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(systemCode,
                exclusiveStates);

        // Filter out the document being updated if excludeId is provided
        if (excludeId != null) {
            existingDocs = existingDocs.stream()
                    .filter(doc -> !doc.getId().equals(excludeId))
                    .toList();
        }

        if (!existingDocs.isEmpty()) {
            String existingStates = existingDocs.stream()
                    .map(doc -> doc.getDocumentState().toString())
                    .collect(Collectors.joining(", "));
            throw new IllegalOperationException(
                    String.format("Cannot create/update document for system %s. " +
                            "Documents already exist in exclusive states: %s. " +
                            "Only one document can be in DRAFT, SUBMITTED, or APPROVED state at a time.",
                            systemCode, existingStates));
        }
    }

    /**
     * Validates that only one document exists in exclusive states for the given
     * system code.
     * 
     * @param systemCode the system code to validate
     * @throws IllegalOperationException if constraint is violated
     */
    public void validateExclusiveStateConstraint(String systemCode) {
        validateExclusiveStateConstraint(systemCode, null);
    }

    /**
     * Validates that only one ACTIVE document exists for the given system code.
     * 
     * @param systemCode the system code to validate
     * @param excludeId  optional document ID to exclude from the check (for
     *                   updates)
     * @throws IllegalOperationException if constraint is violated
     */
    public void validateActiveStateConstraint(String systemCode, String excludeId) {
        List<SolutionReview> activeDocs = solutionReviewRepository.findAllBySystemCodeAndDocumentStateIn(systemCode,
                List.of(DocumentState.ACTIVE));

        // Filter out the document being updated if excludeId is provided
        if (excludeId != null) {
            activeDocs = activeDocs.stream()
                    .filter(doc -> !doc.getId().equals(excludeId))
                    .toList();
        }

        if (!activeDocs.isEmpty()) {
            throw new IllegalOperationException(
                    String.format("Cannot create/update document for system %s. " +
                            "An ACTIVE document already exists. " +
                            "Only one document can be in ACTIVE state at a time.",
                            systemCode));
        }
    }

    /**
     * Validates that only one ACTIVE document exists for the given system code.
     * 
     * @param systemCode the system code to validate
     * @throws IllegalOperationException if constraint is violated
     */
    public void validateActiveStateConstraint(String systemCode) {
        validateActiveStateConstraint(systemCode, null);
    }

}
