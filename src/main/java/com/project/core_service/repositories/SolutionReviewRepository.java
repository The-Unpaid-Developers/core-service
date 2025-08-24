package com.project.core_service.repositories;

import com.project.core_service.models.solutions_review.SolutionReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * Retrieves a list of {@link SolutionReview} entries filtered by system code,
     * with results sorted according to the provided {@link Sort} parameter.
     *
     * @param systemCode the system code used for filtering
     * @param sort       the sorting criteria for the results
     * @return a {@link List} of solution reviews that match the given system code,
     *         sorted as specified
     */
    List<SolutionReview> findAllBySystemCode(String systemCode, Sort sort);
}
