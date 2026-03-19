package com.vlz.ladorexchange_vacancyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Employer company profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {

    @Schema(description = "Company ID", example = "5")
    private Long id;

    @Schema(description = "Employer user ID (set automatically from X-User-Id header)", example = "3")
    private Long employerId;

    @Schema(description = "Company name (2–100 characters)", example = "Acme Corp", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Company description (max 2000 characters)", example = "We build software solutions...")
    @Size(max = 2000, message = "Description is too long (max 2000 characters)")
    private String description;

    @Schema(description = "Office location / city", example = "Moscow", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(description = "Contact email", example = "hr@acme.ru", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Contact phone number (+7, +380, or +375 prefix)", example = "+79161234567")
    @Pattern(regexp = "^(\\+7|8|\\+380|\\+375)\\d{9,11}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Schema(description = "Company website URL", example = "https://acme.ru")
    @URL(message = "Website must be a valid URL")
    private String website;
}
