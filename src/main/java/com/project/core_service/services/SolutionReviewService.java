package com.project.core_service.services;

import com.project.core_service.client.ChatbotServiceClient;
import com.project.core_service.dto.ChatbotTranslateResponseDTO;
import com.project.core_service.dto.CleanSolutionReviewDTO;
import com.project.core_service.dto.SearchQueryDTO;
import com.project.core_service.dto.SystemDependencyDTO;
import com.project.core_service.dto.BusinessCapabilityDiagramDTO;
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

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private final ChatbotServiceClient chatbotServiceClient;
    private final QueryService queryService;


    @Autowired
    public SolutionReviewService(SolutionReviewRepository solutionReviewRepository, ChatbotServiceClient chatbotServiceClient, QueryService queryService) {
        this.solutionReviewRepository = solutionReviewRepository;
        this.chatbotServiceClient = chatbotServiceClient;
        this.queryService = queryService;
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

    private CleanSolutionReviewDTO toCleanDTO(SolutionReview review) {
        CleanSolutionReviewDTO dto = new CleanSolutionReviewDTO();
        dto.setId(review.getId());
        dto.setSystemCode(review.getSystemCode());
        dto.setDocumentState(review.getDocumentState());
        dto.setSolutionOverview(review.getSolutionOverview());
        return dto;
    }

    /**
     * Retrieves all {@link SolutionReview} entries.
     *
     * @return a {@link List} of all solution reviews
     */
    public List<CleanSolutionReviewDTO> getAllSolutionReviews() {
        List<SolutionReview> reviews = solutionReviewRepository.findAll(Sort.by(Sort.Direction.DESC, "lastModifiedAt"));
        return reviews.stream()
                .map(this::toCleanDTO)
                .toList();
    }

    /**
     * Retrieves all {@link SolutionReview} entries with pagination.
     *
     * @param pageable the pagination information
     * @return a {@link Page} of solution reviews
     */
    public Page<CleanSolutionReviewDTO> getSolutionReviews(Pageable pageable) {
        Page<SolutionReview> reviewsPage = solutionReviewRepository.findAll(pageable);
        return reviewsPage.map(this::toCleanDTO);
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
    public List<CleanSolutionReviewDTO> getSolutionReviewsBySystemCode(String systemCode) {
        List<SolutionReview> reviews = solutionReviewRepository.findAllBySystemCode(systemCode);
        return reviews.stream()
                .map(this::toCleanDTO)
                .toList();
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
    public Page<CleanSolutionReviewDTO> getPaginatedSystemView(Pageable pageable) {
        List<String> allSystemCodes = solutionReviewRepository.findAllDistinctSystemCodes();

        // Get ALL representative reviews first
        List<CleanSolutionReviewDTO> allRepresentativeReviews = allSystemCodes.stream()
                .map(this::getRepresentativeReviewForSystem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::toCleanDTO)
                .toList();

        // Apply pagination AFTER filtering
        int total = allRepresentativeReviews.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        if (start >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        List<CleanSolutionReviewDTO> paginatedList = allRepresentativeReviews.subList(start, end);
        return new PageImpl<>(paginatedList, pageable, total);
    }

    private Optional<SolutionReview> getRepresentativeReviewForSystem(String systemCode) {
        Optional<SolutionReview> activeReview = solutionReviewRepository.findActiveBySystemCode(systemCode);

        if (activeReview.isPresent()) {
            return activeReview;
        }

        List<SolutionReview> reviews = solutionReviewRepository.findBySystemCode(
                systemCode, Sort.by(Sort.Direction.DESC, "lastModifiedAt"));

        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.getFirst());
    }

    /**
     * Retrieves all {@link SolutionReview} entries with a specific document state,
     * with pagination.
     *
     * @param documentStateStr the document state used for filtering
     * @param pageable      the pagination information
     * @return a {@link Page} of solution reviews with the specified document state
     */
    public Page<CleanSolutionReviewDTO> getSolutionReviewsByDocumentState(String documentStateStr, Pageable pageable) {
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
        Page<SolutionReview> reviews = solutionReviewRepository.findByDocumentState(documentState, pageable);
        return reviews.map(this::toCleanDTO);
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
    public List<BusinessCapabilityDiagramDTO> getBusinessCapabilitySolutionReviews() {
        List<SolutionReview> activeSolutionReviews = solutionReviewRepository.findByDocumentState(DocumentState.ACTIVE);
        return activeSolutionReviews.stream()
                .map(BusinessCapabilityDiagramDTO::fromSolutionReview)
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

        if (systemCode.isBlank()) {
            throw new IllegalArgumentException("System code cannot be blank");
        }

        SolutionOverview overview = solutionOverview.toNewDraftEntity();
        SolutionReview solutionReview = SolutionReview.newDraftBuilder()
                .id(solutionOverview.getSolutionDetails().getSolutionName().replace(" ", "-") + "-1")
                .systemCode(systemCode)
                .solutionOverview(overview)
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
        SolutionOverview overview = SolutionOverview
                .fromExisting(activeSolutionReview.getSolutionOverview())
                .build();
        SolutionReview solutionReview = SolutionReview.fromExisting(activeSolutionReview, null)
                .solutionOverview(overview).build();

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

        if (modifiedSolutionReview.getSolutionOverview() != null) {
            SolutionOverview overview = modifiedSolutionReview.getSolutionOverview();
            updateIfNotEmpty(overview.getConcerns(), overview::setConcerns);
            solutionReview.setSolutionOverview(overview);
        }

        updateIfNotEmpty(modifiedSolutionReview.getBusinessCapabilities(), solutionReview::setBusinessCapabilities);
        updateIfNotEmpty(modifiedSolutionReview.getSystemComponents(), solutionReview::setSystemComponents);
        updateIfNotEmpty(modifiedSolutionReview.getIntegrationFlows(), solutionReview::setIntegrationFlows);
        updateIfNotEmpty(modifiedSolutionReview.getDataAssets(), solutionReview::setDataAssets);
        updateIfNotEmpty(modifiedSolutionReview.getTechnologyComponents(), solutionReview::setTechnologyComponents);
        updateIfNotEmpty(modifiedSolutionReview.getEnterpriseTools(), solutionReview::setEnterpriseTools);
        updateIfNotEmpty(modifiedSolutionReview.getProcessCompliances(), solutionReview::setProcessCompliances);

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
            SolutionOverview overview = modifiedSolutionReview.getSolutionOverview();
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

        solutionReviewRepository.deleteById(id);
    }

    private <T> void updateIfNotEmpty(List<T> entities,  Consumer<List<T>> setter) {
        if (entities != null && !entities.isEmpty()) {
            setter.accept(entities);
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

    /**
     * Searches for solution reviews using natural language query.
     *
     * <p>
     * This method calls the chatbot microservice's /translate endpoint to convert
     * the natural language query into a MongoDB aggregation pipeline and execute it.
     * The results are then used to query the local database for each solution review ID,
     * returning a list of CleanSolutionReviewDTO objects.
     * </p>
     *
     * @param searchQueryDTO the DTO containing the search query
     * @return a {@link List} of {@link CleanSolutionReviewDTO} matching the search criteria
     */
    public List<CleanSolutionReviewDTO> searchSolutionReviews(SearchQueryDTO searchQueryDTO) {
        String query = searchQueryDTO.getSearchQuery();

        // Call chatbot service to translate the query and execute it
        try {
            ChatbotTranslateResponseDTO response = chatbotServiceClient.translate(query, true);
            System.out.println("Chatbot service returned response: " + response.toString());
            System.out.println("Mongo query: " + response.getMongoQuery());

            // execute the returned mongo query against local database
            if (response.getMongoQuery() == null) {
                return List.of();
            }

            List<Document> results = queryService.executeMongoQuery(response.getMongoQuery());
            System.out.println("Results from executed mongo query: " + results.toString());
            System.out.println("Number of results: " + results.size());
            
            return results.stream()
                    .map(doc -> {
                        String id = doc.get("_id").toString();
                        return solutionReviewRepository.findById(id);
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(this::toCleanDTO)
                    .toList();

            // Extract solution review IDs from the results
            // if (response.getResults() == null || response.getResults().isEmpty()) {
            //     return List.of();
            // }
            // System.out.println("results of response: " + response.getResults().toString());
            // // Extract IDs from the results and query the database
            // List<String> ids = response.getResults().stream()
            //         .map(result -> (String) result.get("id"))
            //         .filter(id -> id != null)
            //         .toList();

            // // Query the database for each ID and map to CleanSolutionReviewDTO
            // return ids.stream()
            //         .map(solutionReviewRepository::findById)
            //         .filter(Optional::isPresent)
            //         .map(Optional::get)
            //         .map(this::toCleanDTO)
            //         .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to communicate with chatbot service: " + e.getMessage(), e);
        }
    }
}
