package com.vlz.ladorexchange_vacancyservice.controller;

import com.vlz.ladorexchange_vacancyservice.dto.ReviewDto;
import com.vlz.ladorexchange_vacancyservice.dto.ReviewSummaryDto;
import com.vlz.ladorexchange_vacancyservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Company Reviews", description = "Glassdoor-style company reviews by job seekers")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @Operation(summary = "Get all reviews for a company (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of reviews with average rating")
    @GetMapping("/company/{companyId}")
    public Page<ReviewDto> getByCompany(
            @Parameter(description = "Company ID") @PathVariable Long companyId,
            Pageable pageable) {
        return service.getByCompanyId(companyId, pageable);
    }

    @Operation(summary = "Get review summary (average rating, distribution) for a company")
    @ApiResponse(responseCode = "200", description = "Review summary",
            content = @Content(schema = @Schema(implementation = ReviewSummaryDto.class)))
    @GetMapping("/company/{companyId}/summary")
    public ReviewSummaryDto getSummary(@PathVariable Long companyId) {
        return service.getSummary(companyId);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Leave a review for a company", description = "One review per job seeker per company. JOB_SEEKER role enforced by Gateway.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review created",
                    content = @Content(schema = @Schema(implementation = ReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Already reviewed this company")
    })
    @PostMapping("/company/{companyId}")
    public ReviewDto create(
            @PathVariable Long companyId,
            @RequestBody @Valid ReviewDto dto,
            @Parameter(description = "Authenticated user ID (from Gateway)") @RequestHeader("X-User-Id") Long userId) {
        return service.create(companyId, userId, dto);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update own review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated review"),
            @ApiResponse(responseCode = "403", description = "Not the review author"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PutMapping("/{reviewId}")
    public ReviewDto update(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewDto dto,
            @RequestHeader("X-User-Id") Long userId) {
        return service.update(reviewId, userId, dto);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a review", description = "Author can delete own review; ADMIN can delete any review.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Not the author or admin"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/{reviewId}")
    public void delete(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "JOB_SEEKER") String userRole) {
        service.delete(reviewId, userId, userRole);
    }
}
