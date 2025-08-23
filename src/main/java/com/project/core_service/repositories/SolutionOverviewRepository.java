package com.project.core_service.repositories;

import com.project.core_service.models.solution_overview.SolutionOverview;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionOverviewRepository extends MongoRepository<SolutionOverview, String> {
}
