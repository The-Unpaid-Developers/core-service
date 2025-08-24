package com.project.core_service.repositories;

import com.project.core_service.models.data_asset.DataAsset;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataAssetRepository extends MongoRepository<DataAsset, String> {
}
