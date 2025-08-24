package com.project.core_service.repositories;

import com.project.core_service.models.business_capabilities.BusinessCapability;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessCapabilityRepository extends MongoRepository<BusinessCapability, String> {
}
