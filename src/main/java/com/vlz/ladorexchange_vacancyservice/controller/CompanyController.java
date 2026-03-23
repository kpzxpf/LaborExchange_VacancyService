package com.vlz.ladorexchange_vacancyservice.controller;

import com.vlz.ladorexchange_vacancyservice.dto.CompanyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Company;
import com.vlz.ladorexchange_vacancyservice.mapper.CompanyMapper;
import com.vlz.ladorexchange_vacancyservice.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Companies", description = "Employer company profiles")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService service;
    private final CompanyMapper companyMapper;

    @Operation(summary = "Get all companies")
    @ApiResponse(responseCode = "200", description = "List of all companies",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CompanyDto.class))))
    @GetMapping
    public List<CompanyDto> getAll() {
        return companyMapper.toDtoList(service.getAll());
    }

    @Operation(summary = "Get company by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company found",
                    content = @Content(schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/{id}")
    public CompanyDto getById(
            @Parameter(description = "Company ID", required = true) @PathVariable Long id) {
        return companyMapper.toDto(service.getById(id));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my company", description = "Returns the company belonging to the authenticated employer (identified via `X-User-Id`). Returns 204 if no company found.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company found",
                    content = @Content(schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "204", description = "No company registered for this employer")
    })
    @GetMapping("/my")
    public ResponseEntity<CompanyDto> getMy(
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        return service.findByEmployerId(userId)
                .map(companyMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Get company by employer ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company found",
                    content = @Content(schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "204", description = "No company found for this employer")
    })
    @GetMapping("/employer/{employerId}")
    public ResponseEntity<CompanyDto> getByEmployerId(
            @Parameter(description = "Employer user ID", required = true) @PathVariable Long employerId) {
        return service.findByEmployerId(employerId)
                .map(companyMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a company", description = "Creates a company profile for the authenticated employer. One employer can have one company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company created",
                    content = @Content(schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Employer already has a company")
    })
    @PostMapping
    public CompanyDto create(
            @RequestBody @Valid CompanyDto dto,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        dto.setEmployerId(userId);
        Company company = service.create(dto);
        return companyMapper.toDto(company);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated company",
                    content = @Content(schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the company owner"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PutMapping("/{id}")
    public CompanyDto update(
            @Parameter(description = "Company ID", required = true) @PathVariable Long id,
            @RequestBody @Valid CompanyDto dto,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        return companyMapper.toDto(service.update(id, userId, dto));
    }

    @Operation(summary = "Get company name by vacancy ID", description = "Internal endpoint used by ApplicationService to display company name.")
    @ApiResponse(responseCode = "200", description = "Company name string")
    @GetMapping("/{id}/company")
    public String getCompanyName(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable("id") Long id) {
        return service.getCompanyNameByVacancyId(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the company owner"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "Company ID", required = true) @PathVariable Long id,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        service.delete(id, userId);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Verify a company (Admin only)", description = "Sets isVerified=true. Only ADMIN role (enforced by Gateway).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company verified",
                    content = @Content(schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PatchMapping("/{id}/verify")
    public CompanyDto verify(
            @Parameter(description = "Company ID", required = true) @PathVariable Long id) {
        return service.verify(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Unverify a company (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company unverified",
                    content = @Content(schema = @Schema(implementation = CompanyDto.class))),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PatchMapping("/{id}/unverify")
    public CompanyDto unverify(
            @Parameter(description = "Company ID", required = true) @PathVariable Long id) {
        return service.unverify(id);
    }
}
