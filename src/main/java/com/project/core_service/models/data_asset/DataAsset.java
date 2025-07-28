package com.project.core_service.models.data_asset;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;
import com.project.core_service.models.shared.VersionedSchema;

import lombok.Data;

@Document
@Data
public class DataAsset implements VersionedSchema {
    @Id
    private String id;

    @NonNull
    private String solutionOverviewId;

    // TODO: Potential Enum, but we don't have all the deps name :(
    @NonNull
    private String dataDomain;

    @NonNull
    private Classification dataClassification;

    // TODO: Potential Enum, but we don't have all the deps name :(
    @NonNull
    private String ownedByBusinessUnit;

    @NonNull
    private List<String> dataEntities;

    @NonNull
    private String masteredIn;

    private int version;
}
