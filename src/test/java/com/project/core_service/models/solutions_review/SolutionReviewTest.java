package com.project.core_service.models.solutions_review;

import com.project.core_service.exceptions.IllegalStateTransitionException;
import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.data_asset.DataAsset;
import com.project.core_service.models.enterprise_tools.EnterpriseTool;
import com.project.core_service.models.integration_flow.IntegrationFlow;
import com.project.core_service.models.process_compliance.ProcessCompliant;
import com.project.core_service.models.solution_overview.*;
import com.project.core_service.models.system_component.SystemComponent;
import com.project.core_service.models.technology_component.TechnologyComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SolutionReview class.
 * Tests constructors, state transitions, utility methods, and builder patterns.
 */
class SolutionReviewTest {

    @Mock
    private SolutionOverview mockSolutionOverview;

    @Mock
    private BusinessCapability mockBusinessCapability;

    @Mock
    private SystemComponent mockSystemComponent;

    @Mock
    private IntegrationFlow mockIntegrationFlow;

    @Mock
    private DataAsset mockDataAsset;

    @Mock
    private TechnologyComponent mockTechnologyComponent;

    @Mock
    private EnterpriseTool mockEnterpriseTool;

    @Mock
    private ProcessCompliant mockProcessCompliant;

    private SolutionOverview realSolutionOverview;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        realSolutionOverview = createTestSolutionOverview();
    }

    private SolutionOverview createTestSolutionOverview() {
        SolutionDetails solutionDetails = new SolutionDetails(
                "Test Solution",
                "Test Project",
                "AWG001",
                "John Architect",
                "Jane PM",
                Arrays.asList("Partner1", "Partner2"));

        return new SolutionOverview(
                "overview-1",
                solutionDetails,
                Arrays.asList("Partner1", "Partner2"),
                "john.reviewer",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "No conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.BUSINESS_OR_CUSTOMER_GROWTH,
                "Increase revenue by 10%",
                Arrays.asList(ApplicationUser.CUSTOMERS, ApplicationUser.EMPLOYEE),
                new ArrayList<>()
                );
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Simple constructor should create draft with default values")
        void simpleConstructorShouldCreateDraftWithDefaults() {
            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
            String systemCode = "sys-001";

            SolutionReview review = new SolutionReview(systemCode, realSolutionOverview);

            LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

            assertEquals(systemCode, review.getSystemCode());
            assertEquals(DocumentState.DRAFT, review.getDocumentState());
            assertEquals(realSolutionOverview, review.getSolutionOverview());
            assertNotNull(review.getCreatedAt());
            assertNotNull(review.getLastModifiedAt());
            assertTrue(review.getCreatedAt().isAfter(beforeCreation));
            assertTrue(review.getCreatedAt().isBefore(afterCreation));
            assertTrue(review.getLastModifiedAt().isAfter(beforeCreation));
            assertTrue(review.getLastModifiedAt().isBefore(afterCreation));

            // Verify lists are initialized
            assertNotNull(review.getBusinessCapabilities());
            assertNotNull(review.getSystemComponents());
            assertNotNull(review.getIntegrationFlows());
            assertNotNull(review.getDataAssets());
            assertNotNull(review.getTechnologyComponents());
            assertNotNull(review.getEnterpriseTools());
            assertNotNull(review.getProcessCompliances());

            assertTrue(review.getBusinessCapabilities().isEmpty());
            assertTrue(review.getSystemComponents().isEmpty());
            assertTrue(review.getIntegrationFlows().isEmpty());
            assertTrue(review.getDataAssets().isEmpty());
            assertTrue(review.getTechnologyComponents().isEmpty());
            assertTrue(review.getEnterpriseTools().isEmpty());
            assertTrue(review.getProcessCompliances().isEmpty());
        }

        @Test
        @DisplayName("Builder with state and user should set all provided values")
        void builderWithStateAndUserShouldSetAllProvidedValues() {
            List<BusinessCapability> capabilities = Arrays.asList(mockBusinessCapability);
            List<SystemComponent> components = Arrays.asList(mockSystemComponent);
            List<IntegrationFlow> flows = Arrays.asList(mockIntegrationFlow);
            List<DataAsset> assets = Arrays.asList(mockDataAsset);
            List<TechnologyComponent> techComponents = Arrays.asList(mockTechnologyComponent);
            List<EnterpriseTool> tools = Arrays.asList(mockEnterpriseTool);
            List<ProcessCompliant> compliances = Arrays.asList(mockProcessCompliant);
            String creator = "test.user";

            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

            String systemCode = "sys-001";
            SolutionReview review = SolutionReview.withStateAndUser(DocumentState.SUBMITTED, creator)
                    .systemCode(systemCode)
                    .solutionOverview(realSolutionOverview)
                    .businessCapabilities(capabilities)
                    .systemComponents(components)
                    .integrationFlows(flows)
                    .dataAssets(assets)
                    .technologyComponents(techComponents)
                    .enterpriseTools(tools)
                    .processCompliances(compliances)
                    .build();

            LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

            assertEquals(DocumentState.SUBMITTED, review.getDocumentState());
            assertEquals(realSolutionOverview, review.getSolutionOverview());
            assertEquals(systemCode, review.getSystemCode());
            assertEquals(capabilities, review.getBusinessCapabilities());
            assertEquals(components, review.getSystemComponents());
            assertEquals(flows, review.getIntegrationFlows());
            assertEquals(assets, review.getDataAssets());
            assertEquals(techComponents, review.getTechnologyComponents());
            assertEquals(tools, review.getEnterpriseTools());
            assertEquals(compliances, review.getProcessCompliances());
            assertEquals(creator, review.getCreatedBy());
            assertEquals(creator, review.getLastModifiedBy());
            assertTrue(review.getCreatedAt().isAfter(beforeCreation));
            assertTrue(review.getCreatedAt().isBefore(afterCreation));
            assertTrue(review.getLastModifiedAt().isAfter(beforeCreation));
            assertTrue(review.getLastModifiedAt().isBefore(afterCreation));
        }

        @Test
        @DisplayName("Builder should handle null lists with defaults")
        void builderShouldHandleNullListsWithDefaults() {
            SolutionReview review = SolutionReview.withStateAndUser(DocumentState.DRAFT, "test.user")
                    .systemCode("sys-001")
                    .solutionOverview(realSolutionOverview)
                    .build();

            assertNotNull(review.getSystemCode());
            assertNotNull(review.getBusinessCapabilities());
            assertNotNull(review.getSystemComponents());
            assertNotNull(review.getIntegrationFlows());
            assertNotNull(review.getDataAssets());
            assertNotNull(review.getTechnologyComponents());
            assertNotNull(review.getEnterpriseTools());
            assertNotNull(review.getProcessCompliances());

            assertTrue(review.getBusinessCapabilities().isEmpty());
            assertTrue(review.getSystemComponents().isEmpty());
            assertTrue(review.getIntegrationFlows().isEmpty());
            assertTrue(review.getDataAssets().isEmpty());
            assertTrue(review.getTechnologyComponents().isEmpty());
            assertTrue(review.getEnterpriseTools().isEmpty());
            assertTrue(review.getProcessCompliances().isEmpty());
        }

        @Test
        @DisplayName("Copy constructor should create new version with incremented version")
        void copyConstructorShouldCreateNewVersionWithIncrementedVersion() {
            String systemCode = "sys-001";
            SolutionReview original = new SolutionReview(systemCode, realSolutionOverview);
            original.setCreatedBy("original.creator");
            original.addBusinessCapability(mockBusinessCapability);

            String modifier = "copy.modifier";
            LocalDateTime beforeCopy = LocalDateTime.now().minusSeconds(1);

            SolutionReview copy = new SolutionReview(original, modifier);

            LocalDateTime afterCopy = LocalDateTime.now().plusSeconds(1);

            assertEquals(original.getSystemCode(), copy.getSystemCode());
            assertEquals(original.getDocumentState(), copy.getDocumentState());
            assertEquals(original.getSolutionOverview(), copy.getSolutionOverview());
            assertEquals("original.creator", copy.getCreatedBy()); // preserved
            assertEquals(modifier, copy.getLastModifiedBy()); // updated
            assertEquals(original.getCreatedAt(), copy.getCreatedAt()); // preserved
            assertTrue(copy.getLastModifiedAt().isAfter(beforeCopy));
            assertTrue(copy.getLastModifiedAt().isBefore(afterCopy));

            // Verify lists are deep copied
            assertNotSame(original.getBusinessCapabilities(), copy.getBusinessCapabilities());
            assertEquals(original.getBusinessCapabilities(), copy.getBusinessCapabilities());
        }
    }

    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {

        private SolutionReview review;

        @BeforeEach
        void setUp() {
            review = new SolutionReview("sys-001", realSolutionOverview);
        }

        @Test
        @DisplayName("submit() should transition from DRAFT to SUBMITTED")
        void submitShouldTransitionFromDraftToSubmitted() {
            assertEquals(DocumentState.DRAFT, review.getDocumentState());
            LocalDateTime beforeSubmit = review.getLastModifiedAt();

            review.submit();

            assertEquals(DocumentState.SUBMITTED, review.getDocumentState());
            assertTrue(review.getLastModifiedAt().isAfter(beforeSubmit));
        }

        @Test
        @DisplayName("submit() should throw exception if not in DRAFT state")
        void submitShouldThrowExceptionIfNotInDraftState() {
            review.setDocumentState(DocumentState.CURRENT);

            assertThrows(IllegalStateTransitionException.class, () -> review.submit());
        }

        @Test
        @DisplayName("removeSubmission() should transition from SUBMITTED to DRAFT")
        void removeSubmissionShouldTransitionFromSubmittedToDraft() {
            review.submit(); // DRAFT -> SUBMITTED
            LocalDateTime beforeRemoval = review.getLastModifiedAt();

            review.removeSubmission();

            assertEquals(DocumentState.DRAFT, review.getDocumentState());
            assertTrue(review.getLastModifiedAt().isAfter(beforeRemoval));
        }

        @Test
        @DisplayName("removeSubmission() should throw exception if not in SUBMITTED state")
        void removeSubmissionShouldThrowExceptionIfNotInSubmittedState() {
            assertThrows(IllegalStateTransitionException.class, () -> review.removeSubmission());
        }

        @Test
        @DisplayName("approve() should transition from SUBMITTED to CURRENT")
        void approveShouldTransitionFromSubmittedToCurrent() {
            review.submit(); // DRAFT -> SUBMITTED
            LocalDateTime beforeApproval = review.getLastModifiedAt();

            review.approve();

            assertEquals(DocumentState.CURRENT, review.getDocumentState());
            assertTrue(review.getLastModifiedAt().isAfter(beforeApproval));
        }

        @Test
        @DisplayName("approve() should throw exception if not in SUBMITTED state")
        void approveShouldThrowExceptionIfNotInSubmittedState() {
            assertThrows(IllegalStateTransitionException.class, () -> review.approve());
        }

        @Test
        @DisplayName("unApproveCurrent() should transition from CURRENT to SUBMITTED")
        void unApproveCurrentShouldTransitionFromCurrentToSubmitted() {
            review.submit(); // DRAFT -> SUBMITTED
            review.approve(); // SUBMITTED -> CURRENT
            LocalDateTime beforeUnApproval = review.getLastModifiedAt();

            review.unApproveCurrent();

            assertEquals(DocumentState.SUBMITTED, review.getDocumentState());
            assertTrue(review.getLastModifiedAt().isAfter(beforeUnApproval));
        }

        @Test
        @DisplayName("unApproveCurrent() should throw exception if not in CURRENT state")
        void unApproveCurrentShouldThrowExceptionIfNotInCurrentState() {
            assertThrows(IllegalStateTransitionException.class, () -> review.unApproveCurrent());
        }

        @Test
        @DisplayName("markAsOutdated() should transition from CURRENT to OUTDATED")
        void markAsOutdatedShouldTransitionFromCurrentToOutdated() {
            review.submit(); // DRAFT -> SUBMITTED
            review.approve(); // SUBMITTED -> CURRENT
            LocalDateTime beforeOutdating = review.getLastModifiedAt();

            review.markAsOutdated();

            assertEquals(DocumentState.OUTDATED, review.getDocumentState());
            assertTrue(review.getLastModifiedAt().isAfter(beforeOutdating));
        }

        @Test
        @DisplayName("markAsOutdated() should throw exception if not in CURRENT state")
        void markAsOutdatedShouldThrowExceptionIfNotInCurrentState() {
            assertThrows(IllegalStateTransitionException.class, () -> review.markAsOutdated());
        }

        @Test
        @DisplayName("resetAsCurrent() should transition from OUTDATED to CURRENT")
        void resetAsCurrentShouldTransitionFromOutdatedToCurrent() {
            review.submit(); // DRAFT -> SUBMITTED
            review.approve(); // SUBMITTED -> CURRENT
            review.markAsOutdated(); // CURRENT -> OUTDATED
            LocalDateTime beforeReset = review.getLastModifiedAt();

            review.resetAsCurrent();

            assertEquals(DocumentState.CURRENT, review.getDocumentState());
            assertTrue(review.getLastModifiedAt().isAfter(beforeReset));
        }

        @Test
        @DisplayName("resetAsCurrent() should throw exception if not in OUTDATED state")
        void resetAsCurrentShouldThrowExceptionIfNotInOutdatedState() {
            assertThrows(IllegalStateTransitionException.class, () -> review.resetAsCurrent());
        }

        @Test
        @DisplayName("transitionTo() should transition to valid state with modifier tracking")
        void transitionToShouldTransitionToValidStateWithModifierTracking() {
            String modifier = "test.modifier";
            LocalDateTime beforeTransition = review.getLastModifiedAt();

            review.transitionTo(DocumentState.SUBMITTED, modifier);

            assertEquals(DocumentState.SUBMITTED, review.getDocumentState());
            assertEquals(modifier, review.getLastModifiedBy());
            assertTrue(review.getLastModifiedAt().isAfter(beforeTransition));
        }

        @Test
        @DisplayName("transitionTo() should throw exception for invalid transition")
        void transitionToShouldThrowExceptionForInvalidTransition() {
            assertThrows(IllegalStateTransitionException.class,
                    () -> review.transitionTo(DocumentState.CURRENT, "test.modifier"));
        }
    }

    @Nested
    @DisplayName("State Query Tests")
    class StateQueryTests {

        @Test
        @DisplayName("isDraft() should return true only for DRAFT state")
        void isDraftShouldReturnTrueOnlyForDraftState() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);

            assertTrue(review.isDraft());
            assertFalse(review.isSubmitted());
            assertFalse(review.isCurrent());
            assertFalse(review.isOutdated());
        }

        @Test
        @DisplayName("isSubmitted() should return true only for SUBMITTED state")
        void isSubmittedShouldReturnTrueOnlyForSubmittedState() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);
            review.submit();

            assertFalse(review.isDraft());
            assertTrue(review.isSubmitted());
            assertFalse(review.isCurrent());
            assertFalse(review.isOutdated());
        }

        @Test
        @DisplayName("isCurrent() should return true only for CURRENT state")
        void isCurrentShouldReturnTrueOnlyForCurrentState() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);
            review.submit();
            review.approve();

            assertFalse(review.isDraft());
            assertFalse(review.isSubmitted());
            assertTrue(review.isCurrent());
            assertFalse(review.isOutdated());
        }

        @Test
        @DisplayName("isOutdated() should return true only for OUTDATED state")
        void isOutdatedShouldReturnTrueOnlyForOutdatedState() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);
            review.submit();
            review.approve();
            review.markAsOutdated();

            assertFalse(review.isDraft());
            assertFalse(review.isSubmitted());
            assertFalse(review.isCurrent());
            assertTrue(review.isOutdated());
        }
    }

    @Nested
    @DisplayName("List Management Tests")
    class ListManagementTests {

        private SolutionReview review;

        @BeforeEach
        void setUp() {
            review = new SolutionReview("sys-001", realSolutionOverview);
        }

        @Test
        @DisplayName("addBusinessCapability() should add capability and update modification time")
        void addBusinessCapabilityShouldAddCapabilityAndUpdateModificationTime() {
            LocalDateTime beforeAdd = review.getLastModifiedAt();

            review.addBusinessCapability(mockBusinessCapability);

            assertTrue(review.getBusinessCapabilities().contains(mockBusinessCapability));
            assertEquals(1, review.getBusinessCapabilities().size());
            assertTrue(review.getLastModifiedAt().isAfter(beforeAdd));
        }

        @Test
        @DisplayName("addSystemComponent() should add component and update modification time")
        void addSystemComponentShouldAddComponentAndUpdateModificationTime() {
            LocalDateTime beforeAdd = review.getLastModifiedAt();

            review.addSystemComponent(mockSystemComponent);

            assertTrue(review.getSystemComponents().contains(mockSystemComponent));
            assertEquals(1, review.getSystemComponents().size());
            assertTrue(review.getLastModifiedAt().isAfter(beforeAdd));
        }

        @Test
        @DisplayName("addIntegrationFlow() should add flow and update modification time")
        void addIntegrationFlowShouldAddFlowAndUpdateModificationTime() {
            LocalDateTime beforeAdd = review.getLastModifiedAt();

            review.addIntegrationFlow(mockIntegrationFlow);

            assertTrue(review.getIntegrationFlows().contains(mockIntegrationFlow));
            assertEquals(1, review.getIntegrationFlows().size());
            assertTrue(review.getLastModifiedAt().isAfter(beforeAdd));
        }

        @Test
        @DisplayName("addDataAsset() should add asset and update modification time")
        void addDataAssetShouldAddAssetAndUpdateModificationTime() {
            LocalDateTime beforeAdd = review.getLastModifiedAt();

            review.addDataAsset(mockDataAsset);

            assertTrue(review.getDataAssets().contains(mockDataAsset));
            assertEquals(1, review.getDataAssets().size());
            assertTrue(review.getLastModifiedAt().isAfter(beforeAdd));
        }

        @Test
        @DisplayName("addTechnologyComponent() should add component and update modification time")
        void addTechnologyComponentShouldAddComponentAndUpdateModificationTime() {
            LocalDateTime beforeAdd = review.getLastModifiedAt();

            review.addTechnologyComponent(mockTechnologyComponent);

            assertTrue(review.getTechnologyComponents().contains(mockTechnologyComponent));
            assertEquals(1, review.getTechnologyComponents().size());
            assertTrue(review.getLastModifiedAt().isAfter(beforeAdd));
        }

        @Test
        @DisplayName("addEnterpriseTool() should add tool and update modification time")
        void addEnterpriseToolShouldAddToolAndUpdateModificationTime() {
            LocalDateTime beforeAdd = review.getLastModifiedAt();

            review.addEnterpriseTool(mockEnterpriseTool);

            assertTrue(review.getEnterpriseTools().contains(mockEnterpriseTool));
            assertEquals(1, review.getEnterpriseTools().size());
            assertTrue(review.getLastModifiedAt().isAfter(beforeAdd));
        }

        @Test
        @DisplayName("addProcessCompliance() should add compliance and update modification time")
        void addProcessComplianceShouldAddComplianceAndUpdateModificationTime() {
            LocalDateTime beforeAdd = review.getLastModifiedAt();

            review.addProcessCompliance(mockProcessCompliant);

            assertTrue(review.getProcessCompliances().contains(mockProcessCompliant));
            assertEquals(1, review.getProcessCompliances().size());
            assertTrue(review.getLastModifiedAt().isAfter(beforeAdd));
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("updateModification() should update modification time and modifier")
        void updateModificationShouldUpdateModificationTimeAndModifier() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);
            LocalDateTime beforeUpdate = review.getLastModifiedAt();
            String modifier = "test.modifier";

            review.updateModification(modifier);

            assertEquals(modifier, review.getLastModifiedBy());
            assertTrue(review.getLastModifiedAt().isAfter(beforeUpdate));
        }

    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("newDraftBuilder() should create builder with draft defaults")
        void newDraftBuilderShouldCreateBuilderWithDraftDefaults() {
            LocalDateTime beforeBuild = LocalDateTime.now().minusSeconds(1);

            SolutionReview review = SolutionReview.newDraftBuilder()
                    .systemCode("sys-001")
                    .solutionOverview(realSolutionOverview)
                    .createdBy("test.creator")
                    .build();

            LocalDateTime afterBuild = LocalDateTime.now().plusSeconds(1);

            assertEquals("sys-001", review.getSystemCode());
            assertEquals(DocumentState.DRAFT, review.getDocumentState());
            assertEquals(realSolutionOverview, review.getSolutionOverview());
            assertEquals("test.creator", review.getCreatedBy());
            assertNotNull(review.getCreatedAt());
            assertNotNull(review.getLastModifiedAt());
            assertTrue(review.getCreatedAt().isAfter(beforeBuild));
            assertTrue(review.getCreatedAt().isBefore(afterBuild));
            assertTrue(review.getLastModifiedAt().isAfter(beforeBuild));
            assertTrue(review.getLastModifiedAt().isBefore(afterBuild));
        }

        @Test
        @DisplayName("builderFromSolutionOverview() should create builder with solution overview")
        void builderFromSolutionOverviewShouldCreateBuilderWithSolutionOverview() {
            SolutionReview review = SolutionReview.builderFromSolutionOverview(realSolutionOverview)
                    .systemCode("sys-001")
                    .createdBy("test.creator")
                    .build();

            assertEquals("sys-001", review.getSystemCode());
            assertEquals(DocumentState.DRAFT, review.getDocumentState());
            assertEquals(realSolutionOverview, review.getSolutionOverview());
            assertEquals("test.creator", review.getCreatedBy());
        }

        @Test
        @DisplayName("Builder should handle all list collections properly")
        void builderShouldHandleAllListCollectionsProperly() {
            List<BusinessCapability> capabilities = Arrays.asList(mockBusinessCapability);
            List<SystemComponent> components = Arrays.asList(mockSystemComponent);

            SolutionReview review = SolutionReview.builder()
                    .systemCode("sys-001")
                    .documentState(DocumentState.SUBMITTED)
                    .solutionOverview(realSolutionOverview)
                    .businessCapabilities(capabilities)
                    .systemComponents(components)
                    .createdBy("test.builder")
                    .build();

            assertEquals("sys-001", review.getSystemCode());
            assertEquals(DocumentState.SUBMITTED, review.getDocumentState());
            assertEquals(realSolutionOverview, review.getSolutionOverview());
            assertEquals(capabilities, review.getBusinessCapabilities());
            assertEquals(components, review.getSystemComponents());
            assertEquals("test.builder", review.getCreatedBy());

            // Verify other lists are initialized with defaults
            assertNotNull(review.getIntegrationFlows());
            assertNotNull(review.getDataAssets());
            assertNotNull(review.getTechnologyComponents());
            assertNotNull(review.getEnterpriseTools());
            assertNotNull(review.getProcessCompliances());
            assertTrue(review.getIntegrationFlows().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should handle rapid state transitions correctly")
        void shouldHandleRapidStateTransitionsCorrectly() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);

            // Complete lifecycle
            review.submit();
            assertEquals(DocumentState.SUBMITTED, review.getDocumentState());

            review.approve();
            assertEquals(DocumentState.CURRENT, review.getDocumentState());

            review.markAsOutdated();
            assertEquals(DocumentState.OUTDATED, review.getDocumentState());

            review.resetAsCurrent();
            assertEquals(DocumentState.CURRENT, review.getDocumentState());

            review.unApproveCurrent();
            assertEquals(DocumentState.SUBMITTED, review.getDocumentState());

            review.removeSubmission();
            assertEquals(DocumentState.DRAFT, review.getDocumentState());
        }

        @Test
        @DisplayName("Should maintain data integrity during copy operations")
        void shouldMaintainDataIntegrityDuringCopyOperations() {
            SolutionReview original = new SolutionReview("sys-001", realSolutionOverview);
            original.addBusinessCapability(mockBusinessCapability);
            original.addSystemComponent(mockSystemComponent);

            SolutionReview copy = new SolutionReview(original, "copy.creator");

            // Verify independence of lists
            copy.addBusinessCapability(mock(BusinessCapability.class));

            assertEquals(1, original.getBusinessCapabilities().size());
            assertEquals(2, copy.getBusinessCapabilities().size());

            // Verify shared references where appropriate
            assertSame(original.getSolutionOverview(), copy.getSolutionOverview());
        }

        @Test
        @DisplayName("Should handle null modifiers gracefully")
        void shouldHandleNullModifiersGracefully() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);

            assertDoesNotThrow(() -> review.transitionTo(DocumentState.SUBMITTED, null));
            assertNull(review.getLastModifiedBy());

            assertDoesNotThrow(() -> review.updateModification(null));
            assertNull(review.getLastModifiedBy());
        }

        @Test
        @DisplayName("Should preserve timestamps correctly across operations")
        void shouldPreserveTimestampsCorrectlyAcrossOperations() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);

            LocalDateTime originalCreatedAt = review.getCreatedAt();
            LocalDateTime originalModifiedAt = review.getLastModifiedAt();

            // Perform the operation that should update lastModifiedAt
            review.submit();

            // Verify createdAt is preserved and lastModifiedAt is updated
            assertEquals(originalCreatedAt, review.getCreatedAt()); // Should not change
            assertNotEquals(originalModifiedAt, review.getLastModifiedAt()); // Should be different

            // Additional verification that the modification time is reasonable (within last
            // few seconds)
            assertTrue(review.getLastModifiedAt().isAfter(originalModifiedAt) ||
                    review.getLastModifiedAt().isEqual(originalModifiedAt.plusNanos(1000))); // Should update or be very
                                                                                             // close
        }
    }

    @Test
    @DisplayName("getAvailableOperations should return correct operations based on current state")
    void getAvailableOperationsShouldReturnCorrectOperationsBasedOnCurrentState() {
        SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);

        // DRAFT state should have SUBMIT operation available
        var draftOperations = review.getAvailableOperations();
        assertEquals(1, draftOperations.size());
        assertTrue(draftOperations.contains(DocumentState.StateOperation.SUBMIT));

        // Move to SUBMITTED state
        review.submit();
        var submittedOperations = review.getAvailableOperations();
        assertEquals(2, submittedOperations.size());
        assertTrue(submittedOperations.contains(DocumentState.StateOperation.REMOVE_SUBMISSION));
        assertTrue(submittedOperations.contains(DocumentState.StateOperation.APPROVE));

        // Move to CURRENT state
        review.approve();
        var currentOperations = review.getAvailableOperations();
        assertEquals(2, currentOperations.size());
        assertTrue(currentOperations.contains(DocumentState.StateOperation.UNAPPROVE));
        assertTrue(currentOperations.contains(DocumentState.StateOperation.MARK_OUTDATED));

        // Move to OUTDATED state
        review.markAsOutdated();
        var outdatedOperations = review.getAvailableOperations();
        assertEquals(1, outdatedOperations.size());
        assertTrue(outdatedOperations.contains(DocumentState.StateOperation.RESET_CURRENT));
    }

    @Nested
    @DisplayName("Additional Coverage Tests")
    class AdditionalCoverageTests {

        @Test
        @DisplayName("Should handle default constructor correctly")
        void shouldHandleDefaultConstructorCorrectly() {
            SolutionReview review = new SolutionReview();

            assertNull(review.getId());
            assertNull(review.getDocumentState());
            assertNull(review.getSolutionOverview());
            // Lists are initialized as empty lists due to @Builder.Default annotations
            assertNotNull(review.getBusinessCapabilities());
            assertNotNull(review.getSystemComponents());
            assertNotNull(review.getIntegrationFlows());
            assertNotNull(review.getDataAssets());
            assertNotNull(review.getTechnologyComponents());
            assertNotNull(review.getEnterpriseTools());
            assertNotNull(review.getProcessCompliances());
            assertTrue(review.getBusinessCapabilities().isEmpty());
            assertTrue(review.getSystemComponents().isEmpty());
            assertTrue(review.getIntegrationFlows().isEmpty());
            assertTrue(review.getDataAssets().isEmpty());
            assertTrue(review.getTechnologyComponents().isEmpty());
            assertTrue(review.getEnterpriseTools().isEmpty());
            assertTrue(review.getProcessCompliances().isEmpty());
            assertNull(review.getCreatedAt());
            assertNull(review.getLastModifiedAt());
            assertNull(review.getCreatedBy());
            assertNull(review.getLastModifiedBy());
        }

        @Test
        @DisplayName("Should handle complete builder correctly")
        void shouldHandleCompleteBuilderCorrectly() {
            LocalDateTime beforeBuild = LocalDateTime.now().minusSeconds(1);

            SolutionReview review = SolutionReview.completeBuilder()
                    .id("complete-test")
                    .systemCode("sys-001")
                    .documentState(DocumentState.CURRENT)
                    .solutionOverview(realSolutionOverview)
                    .businessCapabilities(Arrays.asList(mockBusinessCapability))
                    .createdBy("complete.creator")
                    .lastModifiedBy("complete.modifier")
                    .build();

            LocalDateTime afterBuild = LocalDateTime.now().plusSeconds(1);

            assertEquals("complete-test", review.getId());
            assertEquals("sys-001", review.getSystemCode());
            assertEquals(DocumentState.CURRENT, review.getDocumentState());
            assertEquals(realSolutionOverview, review.getSolutionOverview());
            assertEquals("complete.creator", review.getCreatedBy());
            assertEquals("complete.modifier", review.getLastModifiedBy());
            assertTrue(review.getCreatedAt().isAfter(beforeBuild));
            assertTrue(review.getCreatedAt().isBefore(afterBuild));
            assertTrue(review.getLastModifiedAt().isAfter(beforeBuild));
            assertTrue(review.getLastModifiedAt().isBefore(afterBuild));
            assertEquals(1, review.getBusinessCapabilities().size());
        }

        @Test
        @DisplayName("Should handle all setters correctly")
        void shouldHandleAllSettersCorrectly() {
            SolutionReview review = new SolutionReview();
            LocalDateTime testTime = LocalDateTime.now();

            review.setId("setter-test");
            review.setDocumentState(DocumentState.SUBMITTED);
            review.setSolutionOverview(realSolutionOverview);
            review.setBusinessCapabilities(Arrays.asList(mockBusinessCapability));
            review.setSystemComponents(Arrays.asList(mockSystemComponent));
            review.setIntegrationFlows(Arrays.asList(mockIntegrationFlow));
            review.setDataAssets(Arrays.asList(mockDataAsset));
            review.setTechnologyComponents(Arrays.asList(mockTechnologyComponent));
            review.setEnterpriseTools(Arrays.asList(mockEnterpriseTool));
            review.setProcessCompliances(Arrays.asList(mockProcessCompliant));
            review.setCreatedAt(testTime);
            review.setLastModifiedAt(testTime);
            review.setCreatedBy("setter.creator");
            review.setLastModifiedBy("setter.modifier");

            assertEquals("setter-test", review.getId());
            assertEquals(DocumentState.SUBMITTED, review.getDocumentState());
            assertEquals(realSolutionOverview, review.getSolutionOverview());
            assertEquals(1, review.getBusinessCapabilities().size());
            assertEquals(1, review.getSystemComponents().size());
            assertEquals(1, review.getIntegrationFlows().size());
            assertEquals(1, review.getDataAssets().size());
            assertEquals(1, review.getTechnologyComponents().size());
            assertEquals(1, review.getEnterpriseTools().size());
            assertEquals(1, review.getProcessCompliances().size());
            assertEquals(testTime, review.getCreatedAt());
            assertEquals(testTime, review.getLastModifiedAt());
            assertEquals("setter.creator", review.getCreatedBy());
            assertEquals("setter.modifier", review.getLastModifiedBy());
        }

        @Test
        @DisplayName("Should handle toString correctly")
        void shouldHandleToStringCorrectly() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);

            String result = review.toString();

            assertNotNull(result);
            assertTrue(result.contains("SolutionReview"));
        }

        @Test
        @DisplayName("Should handle equals and hashCode correctly")
        void shouldHandleEqualsAndHashCodeCorrectly() {
            SolutionReview review1 = SolutionReview.builder()
                    .id("test-equals")
                    .systemCode("sys-001")
                    .documentState(DocumentState.DRAFT)
                    .solutionOverview(realSolutionOverview)
                    .build();

            SolutionReview review2 = SolutionReview.builder()
                    .id("test-equals")
                    .systemCode("sys-001")
                    .documentState(DocumentState.DRAFT)
                    .solutionOverview(realSolutionOverview)
                    .build();

            assertEquals(review1, review2);
            assertEquals(review1.hashCode(), review2.hashCode());

            // Test inequality
            review2.setDocumentState(DocumentState.SUBMITTED);
            assertNotEquals(review1, review2);
        }

        @Test
        @DisplayName("Should handle multiple operations on lists without interference")
        void shouldHandleMultipleOperationsOnListsWithoutInterference() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);
            LocalDateTime beforeOperations = review.getLastModifiedAt();

            // Add multiple items to different lists
            review.addBusinessCapability(mockBusinessCapability);
            LocalDateTime afterFirstAdd = review.getLastModifiedAt();
            assertTrue(afterFirstAdd.isAfter(beforeOperations));

            review.addSystemComponent(mockSystemComponent);
            LocalDateTime afterSecondAdd = review.getLastModifiedAt();
            assertTrue(afterSecondAdd.isAfter(afterFirstAdd));

            review.addIntegrationFlow(mockIntegrationFlow);
            review.addDataAsset(mockDataAsset);
            review.addTechnologyComponent(mockTechnologyComponent);
            review.addEnterpriseTool(mockEnterpriseTool);
            review.addProcessCompliance(mockProcessCompliant);

            // Verify all lists have the correct size
            assertEquals(1, review.getBusinessCapabilities().size());
            assertEquals(1, review.getSystemComponents().size());
            assertEquals(1, review.getIntegrationFlows().size());
            assertEquals(1, review.getDataAssets().size());
            assertEquals(1, review.getTechnologyComponents().size());
            assertEquals(1, review.getEnterpriseTools().size());
            assertEquals(1, review.getProcessCompliances().size());

            // Verify modification time was updated
            assertTrue(review.getLastModifiedAt().isAfter(afterSecondAdd));
        }

        @Test
        @DisplayName("Should handle state transitions with different user modifiers")
        void shouldHandleStateTransitionsWithDifferentUserModifiers() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);
            review.setCreatedBy("original.creator");

            // Test transitionTo with different users
            review.transitionTo(DocumentState.SUBMITTED, "user1");
            assertEquals("user1", review.getLastModifiedBy());
            assertEquals("original.creator", review.getCreatedBy()); // Should remain unchanged

            review.transitionTo(DocumentState.DRAFT, "user2");
            assertEquals("user2", review.getLastModifiedBy());
            assertEquals("original.creator", review.getCreatedBy()); // Should remain unchanged

            // Test updateModification
            review.updateModification("user3");
            assertEquals("user3", review.getLastModifiedBy());
            assertEquals("original.creator", review.getCreatedBy()); // Should remain unchanged
        }

        @Test
        @DisplayName("Should handle edge cases in copy constructor")
        void shouldHandleEdgeCasesInCopyConstructor() {
            // Create original with extreme values
            SolutionReview original = new SolutionReview("sys-001", realSolutionOverview);
            LocalDateTime veryOldTime = LocalDateTime.of(2000, 1, 1, 0, 0);
            original.setCreatedAt(veryOldTime);
            original.setCreatedBy("ancient.creator");

            // Create copy
            SolutionReview copy = new SolutionReview(original, "new.modifier");

            // Verify version increment works even with large numbers
            assertEquals(veryOldTime, copy.getCreatedAt()); // Preserved
            assertEquals("ancient.creator", copy.getCreatedBy()); // Preserved
            assertEquals("new.modifier", copy.getLastModifiedBy()); // Updated
            assertTrue(copy.getLastModifiedAt().isAfter(veryOldTime)); // Updated
        }

        @Test
        @DisplayName("Should maintain consistency between state methods and enum")
        void shouldMaintainConsistencyBetweenStateMethodsAndEnum() {
            SolutionReview review = new SolutionReview("sys-001", realSolutionOverview);

            // Test all states and their corresponding methods
            review.setDocumentState(DocumentState.DRAFT);
            assertTrue(review.isDraft());
            assertFalse(review.isSubmitted());
            assertFalse(review.isCurrent());
            assertFalse(review.isOutdated());

            review.setDocumentState(DocumentState.SUBMITTED);
            assertFalse(review.isDraft());
            assertTrue(review.isSubmitted());
            assertFalse(review.isCurrent());
            assertFalse(review.isOutdated());

            review.setDocumentState(DocumentState.CURRENT);
            assertFalse(review.isDraft());
            assertFalse(review.isSubmitted());
            assertTrue(review.isCurrent());
            assertFalse(review.isOutdated());

            review.setDocumentState(DocumentState.OUTDATED);
            assertFalse(review.isDraft());
            assertFalse(review.isSubmitted());
            assertFalse(review.isCurrent());
            assertTrue(review.isOutdated());
        }
    }
}
