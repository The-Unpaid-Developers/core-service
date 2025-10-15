package com.project.core_service.models.enterprise_tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class EnterpriseToolTest {
    @Mock
    private Tool tool;

    @Test
    void builderSetsFieldsCorrectly() {

        EnterpriseTool enterpriseTool = EnterpriseTool.builder()
                .id("et-123")
                .tool(tool)
                .onboarded(OnboardingStatus.TRUE)
                .integrationDetails("Integrated with core systems")
                .issues("None")
                .build();

        assertEquals("et-123", enterpriseTool.getId());
        assertEquals(tool, enterpriseTool.getTool());
        assertEquals(OnboardingStatus.TRUE, enterpriseTool.getOnboarded());
        assertEquals("Integrated with core systems", enterpriseTool.getIntegrationDetails());
        assertEquals("None", enterpriseTool.getIssues());
    }

    @Test
    void nonNullFieldsShouldThrowOnNull() {
        EnterpriseTool.EnterpriseToolBuilder builder = EnterpriseTool.builder()
                .id("et-456")
                .onboarded(OnboardingStatus.TRUE)
                .issues("Pending approval");

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void testEnterpriseToolConstructorAndGetters() {
        String enterpriseToolId = UUID.randomUUID().toString();
        EnterpriseTool enterpriseTool = new EnterpriseTool(
                enterpriseToolId,
                tool,
                OnboardingStatus.TRUE,
                "Fully integrated with all pipelines",
                "No issues detected");

        assertEquals(enterpriseToolId, enterpriseTool.getId());
        assertEquals(tool, enterpriseTool.getTool());
        assertEquals(OnboardingStatus.TRUE, enterpriseTool.getOnboarded());
        assertEquals("Fully integrated with all pipelines", enterpriseTool.getIntegrationDetails());
        assertEquals("No issues detected", enterpriseTool.getIssues());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        assertThrows(NullPointerException.class, this::createEnterpriseToolWithNullTool);
        assertThrows(NullPointerException.class, this::createEnterpriseToolWithNullOnboarded);
        assertThrows(NullPointerException.class, this::createEnterpriseToolWithNullIssues);
    }

    private void createEnterpriseToolWithNullTool() {
        new EnterpriseTool("et-002", null, OnboardingStatus.TRUE, "integration status", "issues");
    }

    private void createEnterpriseToolWithNullOnboarded() {
        new EnterpriseTool("et-003", tool, null, "integration status", "issues");
    }

    private void createEnterpriseToolWithNullIssues() {
        new EnterpriseTool("et-004", tool, OnboardingStatus.TRUE, "integration status", null);
    }

    @Test
    void testEnterpriseToolEqualityAndHashCode() {
        String enterpriseToolId = UUID.randomUUID().toString();

        EnterpriseTool et1 = new EnterpriseTool(
                enterpriseToolId, tool, OnboardingStatus.TRUE,
                "Integrated", "No issues");

        EnterpriseTool et2 = new EnterpriseTool(
                enterpriseToolId, tool, OnboardingStatus.TRUE,
                "Integrated", "No issues");

        assertEquals(et1, et2);
        assertEquals(et1.hashCode(), et2.hashCode());
    }

    @Test
    void testEnterpriseToolToString() {
        when(tool.toString()).thenReturn("MockedTool");
        EnterpriseTool enterpriseTool = new EnterpriseTool(
                "enterprise-id", tool, OnboardingStatus.TRUE,
                "Integrated", "No issues");

        String toStringResult = enterpriseTool.toString();
        assertTrue(toStringResult.contains("enterprise-id"));
        assertTrue(toStringResult.contains("MockedTool"));
        assertTrue(toStringResult.contains("TRUE"));
    }
}
