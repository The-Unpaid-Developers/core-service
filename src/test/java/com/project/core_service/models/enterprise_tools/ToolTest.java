package com.project.core_service.models.enterprise_tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ToolTest {

    @Test
    void shouldCreateToolSuccessfully() {
        Tool tool = new Tool(
                "tool-001",
                "Jira",
                ToolType.DEVOPS);

        assertThat(tool.getId()).isEqualTo("tool-001");
        assertThat(tool.getName()).isEqualTo("Jira");
        assertThat(tool.getType()).isEqualTo(ToolType.DEVOPS);
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        assertThatThrownBy(() -> new Tool(
                "tool-001",
                null, // name
                ToolType.DEVOPS)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Tool(
                "tool-001",
                "Jira",
                null // type
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRespectEqualsAndHashCode() {
        Tool a = new Tool(
                "tool-002",
                "Jira",
                ToolType.DEVOPS);

        Tool b = new Tool(
                "tool-002",
                "Jira",
                ToolType.DEVOPS);

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    void toStringShouldContainMeaningfulInfo() {
        Tool tool = new Tool(
                "tool-003",
                "Confluence",
                ToolType.DEVOPS);

        String output = tool.toString();
        assertThat(output).contains(
                "tool-003",
                "Confluence",
                "DEVOPS");
    }
}
