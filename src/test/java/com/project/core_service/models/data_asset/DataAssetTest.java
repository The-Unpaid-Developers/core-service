package com.project.core_service.models.data_asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class DataAssetTest {
    @Test
    void builderSetsFieldsCorrectly() {
        DataAsset asset = DataAsset.builder()
                .id("123")
                .componentName("Component-A")
                .dataDomain("finance")
                .dataClassification(Classification.CONFIDENTIAL)
                .dataOwnership("sales")
                .masteredIn("db-core")
                .build();

        assertEquals("123", asset.getId());
        assertEquals("Component-A", asset.getComponentName());
        assertEquals(Classification.CONFIDENTIAL, asset.getDataClassification());
        assertEquals("finance", asset.getDataDomain());
        assertEquals("sales", asset.getDataOwnership());
        assertEquals("db-core", asset.getMasteredIn());
    }

    @Test
    void builderDefaultsDataEntitiesToEmptyList() {
        DataAsset asset = DataAsset.builder()
                .id("456")
                .componentName("HR-Comp")
                .dataDomain("hr")
                .dataClassification(Classification.INTERNAL)
                .dataOwnership("hr")
                .masteredIn("db-hr")
                .build();

        assertNotNull(asset.getDataEntities());
        assertTrue(asset.getDataEntities().isEmpty());
        assertEquals("HR-Comp", asset.getComponentName());
    }

    @Test
    void nonNullFieldsShouldThrowOnNull() {
        assertThrows(NullPointerException.class, () -> {
            DataAsset.builder()
                    .id("789")
                    .componentName("Ops-Comp")
                    .dataDomain("ops")
                    .dataClassification(Classification.PUBLIC)
                    .dataOwnership("ops")
                    .build();
        });
    }

    @Test
    void shouldCreateDataAssetSuccessfully() {
        DataAsset asset = new DataAsset(
                "da-001",
                "Comp-Finance",
                "Finance",
                Classification.CONFIDENTIAL,
                "Retail Banking",
                List.of("Customer", "Transaction"),
                "Core Banking System"
        );

        assertThat(asset.getId()).isEqualTo("da-001");
        assertThat(asset.getComponentName()).isEqualTo("Comp-Finance");
        assertThat(asset.getDataDomain()).isEqualTo("Finance");
        assertThat(asset.getDataClassification()).isEqualTo(Classification.CONFIDENTIAL);
        assertThat(asset.getDataOwnership()).isEqualTo("Retail Banking");
        assertThat(asset.getDataEntities()).containsExactly("Customer", "Transaction");
        assertThat(asset.getMasteredIn()).isEqualTo("Core Banking System");
    }

    @Test
    void shouldThrowExceptionWhenSettingNullForNonNullFields() {
        assertThatThrownBy(() -> new DataAsset(
                "da-001",
                "Comp-Finance",
                null,
                Classification.CONFIDENTIAL,
                "Retail Banking",
                List.of("Customer", "Transaction"),
                "Core Banking System"
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DataAsset(
                "da-001",
                "Comp-Finance",
                "Finance",
                null,
                "Retail Banking",
                List.of("Customer", "Transaction"),
                "Core Banking System"
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DataAsset(
                "da-001",
                "Comp-Finance",
                "Finance",
                Classification.CONFIDENTIAL,
                null,
                List.of("Customer", "Transaction"),
                "Core Banking System"
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DataAsset(
                "da-001",
                "Comp-Finance",
                "Finance",
                Classification.CONFIDENTIAL,
                "Retail Banking",
                null,
                "Core Banking System"
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DataAsset(
                "da-001",
                "Comp-Finance",
                "Finance",
                Classification.CONFIDENTIAL,
                "Retail Banking",
                List.of("Customer", "Transaction"),
                null
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRespectEqualsAndHashCode() {
        DataAsset a = new DataAsset(
                "same-id",
                "Comp-A",
                "Finance",
                Classification.INTERNAL,
                "Retail Banking",
                List.of("Customer", "Transaction"),
                "Core Banking System"
        );

        DataAsset b = new DataAsset(
                "same-id",
                "Comp-A",
                "Finance",
                Classification.INTERNAL,
                "Retail Banking",
                List.of("Customer", "Transaction"),
                "Core Banking System"
        );

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toStringShouldContainMeaningfulInfo() {
        DataAsset asset = new DataAsset(
                "da-002",
                "Comp-HR",
                "HR",
                Classification.PUBLIC,
                "Human Resources",
                List.of("Employee", "Payroll"),
                "HRMS"
        );

        String output = asset.toString();
        assertThat(output).contains(
                "da-002",
                "Comp-HR",
                "HR",
                "PUBLIC",
                "Human Resources",
                "Employee",
                "Payroll",
                "HRMS"
        );
    }
}
