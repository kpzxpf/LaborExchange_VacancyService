package com.vlz.ladorexchange_vacancyservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {

    private Long id;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String name;

    @Size(max = 2000, message = "Description is too long (max 2000 characters)")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^(\\+7|8|\\+380|\\+375)\\d{9,11}$",
            message = "Invalid phone number format")
    private String phoneNumber;

    @URL(message = "Website must be a valid URL")
    private String website;
}