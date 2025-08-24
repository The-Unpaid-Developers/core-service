package com.project.core_service.repositories;

import com.project.core_service.models.technology_component.TechnologyComponent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnologyComponentRepository extends MongoRepository<TechnologyComponent, String> {
}
