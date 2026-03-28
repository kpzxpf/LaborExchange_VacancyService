package com.vlz.ladorexchange_vacancyservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vlz.ladorexchange_vacancyservice.entity.EmploymentType;
import com.vlz.ladorexchange_vacancyservice.entity.WorkFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Job vacancy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacancyDto {

    @Schema(description = "Vacancy ID", example = "7")
    private long id;

    @Schema(description = "Job title", example = "Senior Java Developer", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Schema(description = "Full job description (max 5000 characters)", example = "We are looking for...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Description required")
    @Size(max = 5000, message = "The description should not exceed 5000 characters.")
    private String description;

    @Schema(description = "Company name", example = "Acme Corp", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Company name is required")
    private String companyName;

    @Schema(description = "Employer user ID (set automatically from X-User-Id header)", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Employer ID is required")
    @Positive(message = "Employer ID must be positive")
    private Long employerId;

    @Schema(description = "Monthly salary in rubles (0 = not specified)", example = "250000.0")
    @PositiveOrZero(message = "Salary cannot be negative")
    private Double salary;

    @Schema(description = "Whether the vacancy is visible in public search", example = "true")
    @JsonProperty("isPublished")
    private Boolean isPublished;

    @Schema(description = "Employment type", example = "FULL_TIME", allowableValues = {"FULL_TIME","PART_TIME","CONTRACT","FREELANCE","INTERNSHIP"})
    private EmploymentType employmentType;

    @Schema(description = "Work format", example = "REMOTE", allowableValues = {"OFFICE","REMOTE","HYBRID"})
    private WorkFormat workFormat;

    @Schema(description = "Creation timestamp", example = "2026-03-20T12:00:00")
    private LocalDateTime createdAt;
}
