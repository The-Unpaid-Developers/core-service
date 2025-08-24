package com.project.core_service.repositories;

import com.project.core_service.models.solutions_review.SolutionReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link SolutionReview} entities.
 *
 * <p>Extends {@link MongoRepository} to provide CRUD operations and query methods
 * for managing solution reviews in a MongoDB database.</p>
 *
 * <p>Includes a custom query method for retrieving reviews by system code with pagination.</p>
 */
@Repository
public interface SolutionReviewRepository extends MongoRepository<SolutionReview, String> {

    /**
     * Retrieves a paginated list of {@link SolutionReview} entries filtered by system code.
     *
     * @param systemCode the system code used for filtering
     * @param pageable   the pagination information
     * @return a {@link Page} of solution reviews that match the given system code
     */
    Page<SolutionReview> findAllBySystemCode(String systemCode, Pageable pageable);
}
