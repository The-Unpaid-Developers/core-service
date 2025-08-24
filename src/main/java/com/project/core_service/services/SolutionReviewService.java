package com.project.core_service.services;

import com.project.core_service.dto.NewSolutionOverviewRequestDTO;
import com.project.core_service.dto.SolutionReviewDTO;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.data_asset.DataAsset;
import com.project.core_service.models.enterprise_tools.EnterpriseTool;
import com.project.core_service.models.enterprise_tools.Tool;
import com.project.core_service.models.integration_flow.IntegrationFlow;
import com.project.core_service.models.process_compliance.ProcessCompliant;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.models.system_component.SystemComponent;
import com.project.core_service.models.technology_component.TechnologyComponent;
import com.project.core_service.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Service layer for managing {@link SolutionReview} entities.
 *
 * <p>This service provides CRUD operations for solution reviews, as well as
 * support for pagination and partial updates. It acts as the business logic
 * layer between the controller and repository.</p>
 */
@Service
public class SolutionReviewService {
    @Autowired
    private SolutionReviewRepository solutionReviewRepository;

    @Autowired
    private SolutionOverviewRepository solutionOverviewRepository;

    @Autowired
    private ConcernRepository concernRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private BusinessCapabilityRepository businessCapabilityRepository;

    @Autowired
    private SystemComponentRepository systemComponentRepository;

    @Autowired
    private IntegrationFlowRepository integrationFlowRepository;

    @Autowired
    private DataAssetRepository dataAssetRepository;

    @Autowired
    private TechnologyComponentRepository technologyComponentRepository;

    @Autowired
    private EnterpriseToolRepository enterpriseToolRepository;

    @Autowired
    private ProcessCompliantRepository processCompliantRepository;


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
     * Creates a new draft {@link SolutionReview}.
     *
     * @param systemCode       the system code associated with the review
     * @param solutionOverview the solution overview details
     * @return the newly created solution review
     */
    public SolutionReview createSolutionReview(String systemCode, NewSolutionOverviewRequestDTO solutionOverview) {
        SolutionOverview savedOverview = saveSolutionOverview(solutionOverview.toNewDraftEntity());
        SolutionReview solutionReview = SolutionReview.newDraftBuilder()
                .systemCode(systemCode)
                .solutionOverview(savedOverview)
                .build();
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
        List<EnterpriseTool> newEnterpriseTools = enterpriseTools.stream()
                .map(enterpriseTool -> {
                    Tool savedTool = toolRepository.save(enterpriseTool.getTool());
                    enterpriseTool.setTool(savedTool);
                    return enterpriseToolRepository.save(enterpriseTool);
                })
                .toList();
        solutionReview.setEnterpriseTools(newEnterpriseTools);
    }

    private <T> void saveIfNotEmpty(List<T> entities, MongoRepository<T, String> repository, Consumer<List<T>> setter) {
        if (entities != null && !entities.isEmpty()) {
            List<T> saved = repository.saveAll(entities);
            setter.accept(saved);
        }
    }
}
