package com.project.core_service.models.data_asset;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Document
@Data
public class DataAsset {
    @Id
    private String id;

    // Potential Enum, but we don't have all the deps name :(
    @NonNull
    private String dataDomain;

    @NonNull
    private Classification dataClassification;

    // Potential Enum, but we don't have all the deps name :(
    @NonNull
    private String ownedByBusinessUnit;

    @NonNull
    private String masteredIn;
}
