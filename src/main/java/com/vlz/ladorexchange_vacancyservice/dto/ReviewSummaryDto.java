package com.vlz.ladorexchange_vacancyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Schema(description = "Aggregated review statistics for a company")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {
    @Schema(description = "Company ID")
    private Long companyId;
    @Schema(description = "Average rating (1.0–5.0)")
    private Double averageRating;
    @Schema(description = "Total number of reviews")
    private Long totalReviews;
    @Schema(description = "Rating distribution (1→count, 2→count, ...5→count)")
    private Map<Integer, Long> ratingDistribution;
}
