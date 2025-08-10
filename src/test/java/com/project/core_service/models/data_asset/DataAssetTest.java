package com.project.core_service.models.data_asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class DataAssetTest {

    @Test
    void shouldCreateDataAssetSuccessfully() {
        DataAsset asset = new DataAsset(
            "da-001",
            "sol-001",
            "Finance",
            Classification.CONFIDENTIAL,
            "Retail Banking",
            List.of("Customer", "Transaction"),
            "Core Banking System",
            1
        );

        assertThat(asset.getId()).isEqualTo("da-001");
        assertThat(asset.getSolutionOverviewId()).isEqualTo("sol-001");
        assertThat(asset.getDataDomain()).isEqualTo("Finance");
        assertThat(asset.getDataClassification()).isEqualTo(Classification.CONFIDENTIAL);
        assertThat(asset.getOwnedByBusinessUnit()).isEqualTo("Retail Banking");
        assertThat(asset.getDataEntities()).containsExactly("Customer", "Transaction");
        assertThat(asset.getMasteredIn()).isEqualTo("Core Banking System");
        assertThat(asset.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenSettingNullForNonNullFields() {
        assertThatThrownBy(() -> new DataAsset(
            "da-001", // id
            null,     // solutionOverviewId
            "Finance",
            Classification.CONFIDENTIAL,
            "Retail Banking",
            List.of("Customer", "Transaction"),
            "Core Banking System",
            1
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DataAsset(
            "da-001",
            "sol-001",
            null,
            Classification.CONFIDENTIAL,
            "Retail Banking",
            List.of("Customer", "Transaction"),
            "Core Banking System",
            1
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DataAsset(
            "da-001",
            "sol-001",
            "Finance",
            null,
            "Retail Banking",
            List.of("Customer", "Transaction"),
            "Core Banking System",
            1
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DataAsset(
            "da-001",
            "sol-001",
            "Finance",
            Classification.CONFIDENTIAL,
            null,
            List.of("Customer", "Transaction"),
            "Core Banking System",
            1
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DataAsset(
            "da-001",
            "sol-001",
            "Finance",
            Classification.CONFIDENTIAL,
            "Retail Banking",
            null,
            "Core Banking System",
            1
        )).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DataAsset(
            "da-001",
            "sol-001",
            "Finance",
            Classification.CONFIDENTIAL,
            "Retail Banking",
            List.of("Customer", "Transaction"),
            null,
            1
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRespectEqualsAndHashCode() {
        DataAsset a = new DataAsset(
            "same-id",
            "sol-001",
            "Finance",
            Classification.INTERNAL,
            "Retail Banking",
            List.of("Customer", "Transaction"),
            "Core Banking System",
            1
        );

        DataAsset b = new DataAsset(
            "same-id",
            "sol-001",
            "Finance",
            Classification.INTERNAL,
            "Retail Banking",
            List.of("Customer", "Transaction"),
            "Core Banking System",
            1
        );

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toStringShouldContainMeaningfulInfo() {
        DataAsset asset = new DataAsset(
            "da-002",
            "sol-002",
            "HR",
            Classification.PUBLIC,
            "Human Resources",
            List.of("Employee", "Payroll"),
            "HRMS",
            1
        );

        String output = asset.toString();
        assertThat(output).contains(
            "da-002",
            "sol-002",
            "HR",
            "PUBLIC",
            "Human Resources",
            "Employee",
            "Payroll",
            "HRMS"
        );
    }
}

