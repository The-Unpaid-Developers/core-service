package com.project.core_service.models.solutions_review;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.exceptions.IllegalStateTransitionException;
import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.data_asset.DataAsset;
import com.project.core_service.models.enterprise_tools.EnterpriseTool;
import com.project.core_service.models.integration_flow.IntegrationFlow;
import com.project.core_service.models.process_compliance.ProcessCompliant;
import com.project.core_service.models.solution_overview.SolutionOverview;
import com.project.core_service.models.system_component.SystemComponent;
import com.project.core_service.models.technology_component.TechnologyComponent;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "solutionReviews")
public class SolutionReview {
    @Id
    private String id;

    @NonNull
    private String systemCode;

    @NonNull
    private DocumentState documentState;

    @NonNull
    @DBRef
    private SolutionOverview solutionOverview;

    @NonNull
    @DBRef
    @Builder.Default
    private List<BusinessCapability> businessCapabilities = new ArrayList<>();

    @NonNull
    @DBRef
    @Builder.Default
    private List<SystemComponent> systemComponents = new ArrayList<>();

    @NonNull
    @DBRef
    @Builder.Default
    private List<IntegrationFlow> integrationFlows = new ArrayList<>();

    @NonNull
    @DBRef
    @Builder.Default
    private List<DataAsset> dataAssets = new ArrayList<>();

    @NonNull
    @DBRef
    @Builder.Default
    private List<TechnologyComponent> technologyComponents = new ArrayList<>();

    @NonNull
    @DBRef
    @Builder.Default
    private List<EnterpriseTool> enterpriseTools = new ArrayList<>();

    @NonNull
    @DBRef
    @Builder.Default
    private List<ProcessCompliant> processCompliances = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

    private String createdBy;

    private String lastModifiedBy;

    // Constructor for creating a new SolutionReview
    public SolutionReview(String systemCode, SolutionOverview solutionOverview) {
        this.documentState = DocumentState.DRAFT;
        this.systemCode = systemCode;
        this.solutionOverview = solutionOverview;
        this.businessCapabilities = new ArrayList<>();
        this.systemComponents = new ArrayList<>();
        this.integrationFlows = new ArrayList<>();
        this.dataAssets = new ArrayList<>();
        this.technologyComponents = new ArrayList<>();
        this.enterpriseTools = new ArrayList<>();
        this.processCompliances = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    // Copy constructor for creating a new version
    public SolutionReview(SolutionReview original, String modifiedBy) {
        this.documentState = original.documentState;
        this.solutionOverview = original.solutionOverview;
        this.systemCode = original.systemCode;
        this.businessCapabilities = new ArrayList<>(original.businessCapabilities);
        this.systemComponents = new ArrayList<>(original.systemComponents);
        this.integrationFlows = new ArrayList<>(original.integrationFlows);
        this.dataAssets = new ArrayList<>(original.dataAssets);
        this.technologyComponents = new ArrayList<>(original.technologyComponents);
        this.enterpriseTools = new ArrayList<>(original.enterpriseTools);
        this.processCompliances = new ArrayList<>(original.processCompliances);
        this.createdAt = original.createdAt;
        this.lastModifiedAt = LocalDateTime.now();
        this.createdBy = original.createdBy;
        this.lastModifiedBy = modifiedBy;
    }

    // Utility methods for document state management

    /**
     * Generic method to execute a state transition operation with validation.
     * 
     * @param operation the operation to perform
     * @throws IllegalStateTransitionException if not in the required state
     */
    private void executeStateOperation(DocumentState.StateOperation operation) {
        this.documentState = this.documentState.executeOperation(operation);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Get all operations that are currently allowed based on the document state.
     * 
     * @return list of operations that can be executed
     */
    public List<DocumentState.StateOperation> getAvailableOperations() {
        return this.documentState.getAvailableOperations();
    }

    /**
     * Submits the document for review.
     * Only documents in DRAFT state can be submitted.
     * 
     * @throws IllegalStateTransitionException if not in DRAFT state
     */
    public void submit(String modifiedBy) {
        executeStateOperation(DocumentState.StateOperation.SUBMIT);
        this.setLastModifiedBy(modifiedBy);
    }

    /**
     * Removes submission and returns document to DRAFT state.
     * Only documents in SUBMITTED state can be returned to draft.
     * 
     * @throws IllegalStateTransitionException if not in SUBMITTED
     *                                         state
     */
    public void removeSubmission(String modifiedBy) {
        executeStateOperation(DocumentState.StateOperation.REMOVE_SUBMISSION);
        this.setLastModifiedBy(modifiedBy);
    }

    /**
     * Approves the document and sets it as current.
     * Only documents in SUBMITTED state can be approved.
     * 
     * @throws IllegalStateTransitionException if not in SUBMITTED
     *                                         state
     */
    public void approve(String modifiedBy) {
        executeStateOperation(DocumentState.StateOperation.APPROVE);
        this.setLastModifiedBy(modifiedBy);
    }

    /**
     * UnApproves a current document and returns it to submitted state.
     * Only documents in CURRENT state can be unapproved.
     * 
     * @throws IllegalStateTransitionException if not in CURRENT state
     */
    public void unApproveCurrent(String modifiedBy) {
        executeStateOperation(DocumentState.StateOperation.UNAPPROVE);
        this.setLastModifiedBy(modifiedBy);
    }

    /**
     * Marks the current document as outdated.
     * Only documents in CURRENT state can be marked as outdated.
     * 
     * @throws IllegalStateTransitionException if not in CURRENT state
     */
    public void markAsOutdated(String modifiedBy) {
        executeStateOperation(DocumentState.StateOperation.MARK_OUTDATED);
        this.setLastModifiedBy(modifiedBy);
    }

    /**
     * Resets an outdated document back to current status.
     * Only documents in OUTDATED state can be reset to current.
     * 
     * @throws IllegalStateTransitionException if not in OUTDATED
     *                                         state
     */
    public void resetAsCurrent(String modifiedBy) {
        executeStateOperation(DocumentState.StateOperation.RESET_CURRENT);
        this.setLastModifiedBy(modifiedBy);
    }

    /**
     * Safely transitions to a new state with validation.
     * 
     * @param newState   the target state to transition to
     * @param modifiedBy the user making the change
     * @throws IllegalStateTransitionException if transition is
     *                                         invalid
     */
    public void transitionTo(DocumentState newState, String modifiedBy) {
        this.documentState.validateTransition(newState);
        this.documentState = newState;
        this.lastModifiedAt = LocalDateTime.now();
        this.lastModifiedBy = modifiedBy;
    }

    public boolean isDraft() {
        return this.documentState == DocumentState.DRAFT;
    }

    public boolean isSubmitted() {
        return this.documentState == DocumentState.SUBMITTED;
    }

    public boolean isCurrent() {
        return this.documentState == DocumentState.CURRENT;
    }

    public boolean isOutdated() {
        return this.documentState == DocumentState.OUTDATED;
    }

    // Utility methods for managing lists
    public void addBusinessCapability(BusinessCapability capability) {
        this.businessCapabilities.add(capability);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addSystemComponent(SystemComponent component) {
        this.systemComponents.add(component);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addIntegrationFlow(IntegrationFlow flow) {
        this.integrationFlows.add(flow);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addDataAsset(DataAsset asset) {
        this.dataAssets.add(asset);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addTechnologyComponent(TechnologyComponent component) {
        this.technologyComponents.add(component);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addEnterpriseTool(EnterpriseTool tool) {
        this.enterpriseTools.add(tool);
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void addProcessCompliance(ProcessCompliant compliant) {
        this.processCompliances.add(compliant);
        this.lastModifiedAt = LocalDateTime.now();
    }

    // Update modification tracking
    public void updateModification(String modifiedBy) {
        this.lastModifiedAt = LocalDateTime.now();
        this.lastModifiedBy = modifiedBy;
    }

    // Builder pattern factory methods
    public static SolutionReviewBuilder newDraftBuilder() {
        return SolutionReview.builder()
                .documentState(DocumentState.DRAFT)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now());
    }

    public static SolutionReviewBuilder builderFromSolutionOverview(SolutionOverview solutionOverview) {
        return newDraftBuilder()
                .solutionOverview(solutionOverview);
    }

    /**
     * Creates a complete SolutionReview with all components.
     * This replaces the complex constructor with a fluent builder approach.
     */
    public static SolutionReviewBuilder completeBuilder() {
        LocalDateTime now = LocalDateTime.now();
        return SolutionReview.builder()
                .createdAt(now)
                .lastModifiedAt(now);
    }

    /**
     * Creates a SolutionReview with specific state and user.
     * Common pattern for creating reviews in specific states.
     */
    public static SolutionReviewBuilder withStateAndUser(DocumentState state, String userId) {
        LocalDateTime now = LocalDateTime.now();
        return SolutionReview.builder()
                .documentState(state)
                .createdAt(now)
                .lastModifiedAt(now)
                .createdBy(userId)
                .lastModifiedBy(userId);
    }

    public static SolutionReviewBuilder fromExisting(SolutionReview original, String modifiedBy) {
        SolutionOverview clonedOverview = SolutionOverview.newEnhancementBuilder(original.solutionOverview).build();
        return SolutionReview.builder()
                .id(null)
                .documentState(DocumentState.DRAFT)
                .solutionOverview(clonedOverview)
                .systemCode(original.systemCode)
                .businessCapabilities(new ArrayList<>(original.businessCapabilities
                        .stream()
                        .peek(businessCapability -> businessCapability.setId(null))
                        .toList())
                )
                .systemComponents(new ArrayList<>(original.systemComponents
                        .stream()
                        .peek(systemComponent -> systemComponent.setId(null))
                        .toList()
                ))
                .integrationFlows(new ArrayList<>(original.integrationFlows
                        .stream()
                        .peek(integrationFlow -> integrationFlow.setId(null))
                        .toList()
                ))
                .dataAssets(new ArrayList<>(original.dataAssets
                        .stream()
                        .peek(dataAsset -> dataAsset.setId(null))
                        .toList()
                ))
                .technologyComponents(new ArrayList<>(original.technologyComponents
                        .stream()
                        .peek(technologyComponent -> technologyComponent.setId(null))
                        .toList()
                ))
                .enterpriseTools(new ArrayList<>(original.enterpriseTools
                        .stream()
                        .peek(enterpriseTool -> enterpriseTool.setId(null))
                        .toList()
                ))
                .processCompliances(new ArrayList<>(original.processCompliances
                        .stream()
                        .peek(processCompliant -> processCompliant.setId(null))
                        .toList()
                ))
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .createdBy(original.createdBy)
                .lastModifiedBy(modifiedBy);
    }
}
