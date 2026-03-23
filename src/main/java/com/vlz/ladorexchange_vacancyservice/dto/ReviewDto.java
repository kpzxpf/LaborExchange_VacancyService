package com.vlz.ladorexchange_vacancyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "Company review left by a job seeker")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    @Schema(description = "Review ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Company ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long companyId;

    @Schema(description = "Author user ID (set from X-User-Id header)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long authorId;

    @Schema(description = "Rating from 1 to 5", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Short rating;

    @Schema(description = "Review title", example = "Great place to work", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Schema(description = "Review body", example = "Good team, flexible hours...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 5000, message = "Text must be between 10 and 5000 characters")
    private String text;

    @Schema(description = "Created timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Average rating for all reviews of the same company (populated in list responses)", accessMode = Schema.AccessMode.READ_ONLY)
    private Double averageRating;

    @Schema(description = "Total number of reviews for the company (populated in list responses)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long totalReviews;
}
