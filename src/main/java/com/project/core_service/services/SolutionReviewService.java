package com.project.core_service.services;

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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service layer for managing {@link SolutionReview} entities.
 *
 * <p>This service provides CRUD operations for solution reviews, as well as
 * support for pagination and partial updates. It acts as the business logic
 * layer between the controller and repository.</p>
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
            ProcessCompliantRepository processCompliantRepository
    ) {
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
     * or empty if no review exists for the given ID
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

    /**
     * Retrieves {@link SolutionReview} entries filtered by system code.
     *
     * @param systemCode the system code used for filtering
     * @return a {@link List} of solution reviews for the given system code
     */
    public List<SolutionReview> getSolutionReviewsBySystemCode(String systemCode) {
        return solutionReviewRepository.findAllBySystemCode(systemCode, Sort.by(Sort.Direction.DESC, "lastModifiedAt"));
    }

   /**
     * Retrieves a paginated view of solution reviews grouped by system.
     *
     * <p>For each system, if an approved solution review exists, it is returned.
     * Otherwise, the latest solution review for the system is returned.</p>
     *
     * @param pageable the pagination information
     * @return a {@link Page} of solution reviews, one per system
     */
    public Page<SolutionReview> getPaginatedSystemView(Pageable pageable) {
        // Retrieve distinct system codes with pagination
        List<String> allSystemCodes = solutionReviewRepository.findAllDistinctSystemCodes();

        // Map each system code to its representative SolutionReview based on business rules
        List<SolutionReview> representativeReviews = allSystemCodes.stream()
            .map(systemCode -> {
                // Check if an approved (CURRENT) solution review exists for the system
                Optional<SolutionReview> approvedReview = solutionReviewRepository.findApprovedBySystemCode(systemCode);
                
                return approvedReview.orElseGet(() -> {
                    // No CURRENT review found, get all reviews for this system sorted by lastModifiedAt DESC
                    List<SolutionReview> reviews = solutionReviewRepository.findBySystemCode(
                        systemCode, Sort.by(Sort.Direction.DESC, "lastModifiedAt")
                    );
                    // Return the latest review (first in DESC order), or null if no reviews exist
                    return reviews.isEmpty() ? null : reviews.get(0);
                });
            })
            .filter(review -> review != null) // Remove any null entries
            .collect(Collectors.toList());
        
        // Apply manual pagination to the representative reviews
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), representativeReviews.size());
        
        // Handle case where start is beyond the available data
        if (start >= representativeReviews.size()) {
            return new PageImpl<>(List.of(), pageable, representativeReviews.size());
        }
        
        List<SolutionReview> paginatedList = representativeReviews.subList(start, end);
        
        return new PageImpl<>(paginatedList, pageable, representativeReviews.size());
    }

    /**
     * Creates a new draft {@link SolutionReview}.
     *
     * @param systemCode       the system code associated with the review
     * @param solutionOverview the solution overview details
     * @return the newly created solution review
     */
    public SolutionReview createSolutionReview(String systemCode, NewSolutionOverviewRequestDTO solutionOverview) {
        List<SolutionReview> solutionReviews = getSolutionReviewsBySystemCode(systemCode);
        if (!solutionReviews.isEmpty() && solutionReviews.getFirst().getDocumentState().ordinal() <= DocumentState.CURRENT.ordinal()) {
            throw new IllegalOperationException("A CURRENT or SUBMITTED or DRAFT review already exists for system " + systemCode);
        }

        if (solutionOverview == null) {
            throw new IllegalArgumentException("SolutionOverview cannot be null");
        }

        SolutionOverview savedOverview = saveSolutionOverview(solutionOverview.toNewDraftEntity());
        SolutionReview solutionReview = SolutionReview.newDraftBuilder()
                .systemCode(systemCode)
                .solutionOverview(savedOverview)
                .build();
        return solutionReviewRepository.insert(solutionReview);
    }

    /**
     * Creates a new draft {@link SolutionReview} for existing systems
     *
     * @param systemCode the system code associated with the review
     * @return the newly created solution review
     */
    public SolutionReview createSolutionReview(String systemCode) {
        List<SolutionReview> solutionReviews = getSolutionReviewsBySystemCode(systemCode);
        if (solutionReviews.isEmpty()) {
            throw new NotFoundException("System " + systemCode + " does not exist");
        }
        SolutionOverview savedOverview = saveSolutionOverview(
                SolutionOverview
                        .fromExisting(solutionReviews.getFirst().getSolutionOverview())
                        .build()
        );
        SolutionReview solutionReview = SolutionReview.fromExisting(solutionReviews.getFirst(), null)
                .solutionOverview(savedOverview).build();

        saveIfNotEmpty(solutionReview.getBusinessCapabilities(), businessCapabilityRepository, solutionReview::setBusinessCapabilities);
        saveIfNotEmpty(solutionReview.getSystemComponents(), systemComponentRepository, solutionReview::setSystemComponents);
        saveIfNotEmpty(solutionReview.getIntegrationFlows(), integrationFlowRepository, solutionReview::setIntegrationFlows);
        saveIfNotEmpty(solutionReview.getDataAssets(), dataAssetRepository, solutionReview::setDataAssets);
        saveIfNotEmpty(solutionReview.getTechnologyComponents(), technologyComponentRepository, solutionReview::setTechnologyComponents);
        saveEnterpriseTools(solutionReview, solutionReview.getEnterpriseTools());
        saveIfNotEmpty(solutionReview.getProcessCompliances(), processCompliantRepository, solutionReview::setProcessCompliances);
        return solutionReviewRepository.insert(solutionReview);
    }

    /**
     * Updates an existing {@link SolutionReview} with partial update logic.
     *
     * <p>Only non-null or non-empty fields from the provided DTO are applied
     * to the existing entity. If the entity does not exist, a
     * {@link NotFoundException} is thrown.</p>
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

        saveIfNotEmpty(modifiedSolutionReview.getBusinessCapabilities(), businessCapabilityRepository, solutionReview::setBusinessCapabilities);
        saveIfNotEmpty(modifiedSolutionReview.getSystemComponents(), systemComponentRepository, solutionReview::setSystemComponents);
        saveIfNotEmpty(modifiedSolutionReview.getIntegrationFlows(), integrationFlowRepository, solutionReview::setIntegrationFlows);
        saveIfNotEmpty(modifiedSolutionReview.getDataAssets(), dataAssetRepository, solutionReview::setDataAssets);
        saveIfNotEmpty(modifiedSolutionReview.getTechnologyComponents(), technologyComponentRepository, solutionReview::setTechnologyComponents);
        saveEnterpriseTools(solutionReview, modifiedSolutionReview.getEnterpriseTools());
        saveIfNotEmpty(modifiedSolutionReview.getProcessCompliances(), processCompliantRepository, solutionReview::setProcessCompliances);

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
        toolRepository.deleteAll(solutionReview.getEnterpriseTools().stream().map(EnterpriseTool::getTool).collect(Collectors.toList()));
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
                    solutionOverview::setConcerns
            );
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
}
