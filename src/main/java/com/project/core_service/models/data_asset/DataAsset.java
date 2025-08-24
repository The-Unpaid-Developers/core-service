package com.project.core_service.models.data_asset;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;

@Document
@Data
@AllArgsConstructor
@Builder
public class DataAsset {
    @Id
    private String id;

    private String componentName;

    // TODO: Potential Enum, but we don't have all the deps name :(
    @NonNull
    private String dataDomain;

    @NonNull
    private Classification dataClassification;

    // Data owned by which department or team
    @NonNull
    private String dataOwnedBy;

    @NonNull
    @Builder.Default
    private List<String> dataEntities = new ArrayList<>();

    @NonNull
    private String masteredIn;
}
