package com.project.core_service.controllers;

import com.project.core_service.dto.NewSolutionOverviewRequestDTO;
import com.project.core_service.dto.SolutionReviewDTO;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.services.SolutionReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing {@link SolutionReview} resources.
 *
 * <p>Provides endpoints for creating, retrieving, updating, and deleting
 * solution reviews. Supports pagination and filtering by system code.</p>
 */
@RestController
@RequestMapping("/api/v1/solution-review")
public class SolutionReviewController {
    private final SolutionReviewService solutionReviewService;

    @Autowired
    public SolutionReviewController(SolutionReviewService solutionReviewService) {
        this.solutionReviewService = solutionReviewService;
    }

    /**
     * Retrieves a {@link SolutionReview} by its ID.
     *
     * @param id the identifier of the solution review
     * @return a {@link ResponseEntity} containing the solution review if found,
     * or {@code 404 Not Found} if no review exists for the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SolutionReview> getSolutionReviewById(@PathVariable String id) {
        return solutionReviewService.getSolutionReviewById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all {@link SolutionReview} entries.
     *
     * @return a {@link ResponseEntity} containing a list of all solution reviews
     */
    @GetMapping
    public ResponseEntity<List<SolutionReview>> getAllSolutionReviews() {
        return ResponseEntity.ok(solutionReviewService.getAllSolutionReviews());
    }

    /**
     * Retrieves all {@link SolutionReview} entries with pagination.
     *
     * @param page the page index (0-based)
     * @param size the number of items per page
     * @return a {@link ResponseEntity} containing a paginated list of solution reviews
     */
    @GetMapping("/paging")
    public ResponseEntity<Page<SolutionReview>> getSolutionReviews(@RequestParam int page, @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(solutionReviewService.getSolutionReviews(pageable));
    }

    /**
     * Retrieves {@link SolutionReview} entries filtered by system code with pagination.
     *
     * @param systemCode the system code used for filtering
     * @return a {@link ResponseEntity} containing a paginated list of solution reviews
     */
    @GetMapping("/system")
    public ResponseEntity<List<SolutionReview>> getSolutionReviewsBySystemCode(@RequestParam String systemCode) {
        return ResponseEntity.ok(solutionReviewService.getSolutionReviewsBySystemCode(systemCode));
    }


    /**
     * Creates a new {@link SolutionReview} for the given system code.
     *
     * @param systemCode       the system code associated with the review
     * @param solutionOverview the solution overview data
     * @return a {@link ResponseEntity} containing the created solution review
     * with status {@code 201 Created}
     */
    @PostMapping("/{systemCode}")
    public ResponseEntity<SolutionReview> createSolutionReview(@PathVariable String systemCode, @RequestBody NewSolutionOverviewRequestDTO solutionOverview) {
        return new ResponseEntity<>(solutionReviewService.createSolutionReview(systemCode, solutionOverview), HttpStatus.CREATED);
    }

    /**
     * Creates a new {@link SolutionReview} for the given system code.
     *
     * @param systemCode       the system code associated with the review
     * @return a {@link ResponseEntity} containing the created solution review
     * with status {@code 201 Created}
     */
    @PostMapping("/existing/{systemCode}")
    public ResponseEntity<SolutionReview> createSolutionReviewFromExisting(@PathVariable String systemCode) {
        return new ResponseEntity<>(solutionReviewService.createSolutionReview(systemCode), HttpStatus.CREATED);
    }

    /**
     * Updates an existing {@link SolutionReview}.
     *
     * @param newSolutionReview the updated solution review object
     * @return a {@link ResponseEntity} containing the updated solution review
     */
    @PutMapping
    public ResponseEntity<SolutionReview> updateSolutionReview(@RequestBody SolutionReviewDTO newSolutionReview) {
        return ResponseEntity.ok(solutionReviewService.updateSolutionReview(newSolutionReview));
    }

    /**
     * Deletes a {@link SolutionReview} by its ID.
     *
     * @param id the identifier of the solution review to delete
     * @return a {@link ResponseEntity} with status {@code 204 No Content}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSolutionReview(@PathVariable String id) {
        solutionReviewService.deleteSolutionReview(id);
        return ResponseEntity.noContent().build();
    }
}
