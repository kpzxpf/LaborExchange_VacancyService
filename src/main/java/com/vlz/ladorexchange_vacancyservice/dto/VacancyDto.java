package com.vlz.ladorexchange_vacancyservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacancyDto {

    private long id;

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @NotBlank(message = "Description required")
    @Size(max = 5000, message = "The description should not exceed 5000 characters.")
    private String description;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Employer ID is required")
    @Positive(message = "Employer ID must be positive")
    private Long employerId;

    @PositiveOrZero(message = "Salary cannot be negative")
    private Double salary;

    private boolean isPublished;
}