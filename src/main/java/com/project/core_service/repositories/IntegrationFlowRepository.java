package com.project.core_service.repositories;

import com.project.core_service.models.integration_flow.IntegrationFlow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationFlowRepository extends MongoRepository<IntegrationFlow, String> {
}
