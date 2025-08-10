package com.project.core_service.models.enterprise_tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ToolTest {

    @Test
    void shouldCreateToolSuccessfully() {
        Tool tool = new Tool(
                "tool-001",
                "Jira",
                ToolType.DEVOPS,
                1
        );

        assertThat(tool.getId()).isEqualTo("tool-001");
        assertThat(tool.getName()).isEqualTo("Jira");
        assertThat(tool.getType()).isEqualTo(ToolType.DEVOPS);
        assertThat(tool.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        assertThatThrownBy(() -> new Tool(
                "tool-001",
                null, // name
                ToolType.DEVOPS,
                1
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Tool(
                "tool-001",
                "Jira",
                null, // type
                1
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRespectEqualsAndHashCode() {
        Tool a = new Tool(
                "tool-002",
                "Jira",
                ToolType.DEVOPS,
                1
        );

        Tool b = new Tool(
                "tool-002",
                "Jira",
                ToolType.DEVOPS,
                1
        );

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toStringShouldContainMeaningfulInfo() {
        Tool tool = new Tool(
                "tool-003",
                "Confluence",
                ToolType.DEVOPS,
                1
        );

        String output = tool.toString();
        assertThat(output).contains(
                "tool-003",
                "Confluence",
                "DEVOPS"
        );
    }
}
