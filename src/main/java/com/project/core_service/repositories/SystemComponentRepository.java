package com.project.core_service.repositories;

import com.project.core_service.models.system_component.SystemComponent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemComponentRepository extends MongoRepository<SystemComponent, String> {
}
