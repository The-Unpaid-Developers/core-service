package com.project.core_service.repositories;

import com.project.core_service.models.enterprise_tools.EnterpriseTool;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnterpriseToolRepository extends MongoRepository<EnterpriseTool, String> {
}
