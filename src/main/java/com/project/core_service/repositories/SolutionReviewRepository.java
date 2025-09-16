package com.project.core_service.repositories;

import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link SolutionReview} entities.
 *
 * <p>
 * Extends {@link MongoRepository} to provide CRUD operations and query methods
 * for managing solution reviews in a MongoDB database.
 * </p>
 *
 * <p>
 * Includes a custom query method for retrieving reviews by system code with
 * pagination.
 * </p>
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

    /**
     * Retrieves the first {@link SolutionReview} entry filtered by system code and
     * document state.
     *
     * @param systemCode     the system code used for filtering
     * @param documentStates the list of document states used for filtering
     * @return an {@link Optional} containing the first solution review that matches
     *         the given criteria, or empty if none found
     */
    Optional<SolutionReview> findFirstBySystemCodeAndDocumentStateIn(String systemCode,
            List<DocumentState> documentStates);

    /**
     * Retrieves a paginated list of distinct system codes.
     *
     * @param pageable the pagination information
     * @return a {@link Page} of distinct system codes
     */        
    @Query(value = "{}", fields = "{ 'systemCode': 1 }")
    Page<String> findDistinctSystemCodes(Pageable pageable);

    /**
     * Retrieves the approved {@link SolutionReview} for a given system code.
     *
     * @param systemCode the system code used for filtering
     * @return an {@link Optional} containing the approved solution review, or empty if none found
     */
    @Query("{ 'systemCode': ?0, 'documentState': 'APPROVED' }")
    Optional<SolutionReview> findApprovedBySystemCode(String systemCode);

     /**
     * Retrieves all {@link SolutionReview} entries for a given system code, sorted by the provided criteria.
     *
     * @param systemCode the system code used for filtering
     * @param sort       the sorting criteria for the results
     * @return a {@link List} of solution reviews for the given system code
     */
    @Query("{ 'systemCode': ?0 }")
    List<SolutionReview> findBySystemCode(String systemCode, Sort sort);
}
