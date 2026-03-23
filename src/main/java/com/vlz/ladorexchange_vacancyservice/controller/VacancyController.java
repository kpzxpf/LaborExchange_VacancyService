package com.vlz.ladorexchange_vacancyservice.controller;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.dto.VacancyViewEvent;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import com.vlz.ladorexchange_vacancyservice.mapper.VacancyMapper;
import com.vlz.ladorexchange_vacancyservice.producer.VacancyViewProducer;
import com.vlz.ladorexchange_vacancyservice.service.VacancyService;
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

import java.util.List;
import java.util.Set;

@Tag(name = "Vacancies", description = "Job vacancy CRUD, skill management, and Elasticsearch indexing")
@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService service;
    private final VacancyMapper vacancyMapper;
    private final VacancyViewProducer viewProducer;

    @Operation(summary = "Get all published vacancies (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of vacancies")
    @GetMapping
    public Page<VacancyDto> getAll(Pageable pageable) {
        Page<Vacancy> vacancyPage = service.getAll(pageable);
        return vacancyPage.map(vacancyMapper::toDto);
    }

    @Operation(summary = "Get vacancy by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacancy found",
                    content = @Content(schema = @Schema(implementation = VacancyDto.class))),
            @ApiResponse(responseCode = "404", description = "Vacancy not found")
    })
    @GetMapping("/{id}")
    public VacancyDto getById(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id,
            @Parameter(description = "Viewer user ID (injected by Gateway, optional)")
            @RequestHeader(value = "X-User-Id", required = false) Long viewerId,
            @Parameter(description = "Viewer role (injected by Gateway, optional)")
            @RequestHeader(value = "X-User-Role", required = false) String viewerRole) {
        VacancyDto dto = service.getById(id);
        viewProducer.send(VacancyViewEvent.builder()
                .vacancyId(id)
                .vacancyTitle(dto.getTitle())
                .employerId(dto.getEmployerId())
                .viewerId(viewerId)
                .viewerRole(viewerRole)
                .viewedAt(java.time.LocalDateTime.now())
                .build());
        return dto;
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Create a vacancy",
            description = "Creates a new job vacancy. Only EMPLOYER role is allowed (enforced by Gateway). The employer ID is taken from `X-User-Id`. Publishes a VacancyIndexEvent to Kafka."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacancy created",
                    content = @Content(schema = @Schema(implementation = VacancyDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden — EMPLOYER role required")
    })
    @PostMapping
    public VacancyDto create(
            @RequestBody @Valid VacancyDto vacancyDto,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        vacancyDto.setEmployerId(userId);
        return vacancyMapper.toDto(service.create(vacancyDto));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a vacancy", description = "Only the employer who owns the vacancy may update it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated vacancy",
                    content = @Content(schema = @Schema(implementation = VacancyDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the vacancy owner"),
            @ApiResponse(responseCode = "404", description = "Vacancy not found")
    })
    @PutMapping("/{id}")
    public VacancyDto update(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id,
            @RequestBody @Valid VacancyDto vacancyDto,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        vacancyDto.setId(id);
        return vacancyMapper.toDto(service.update(vacancyDto, userId));
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Publish a vacancy", description = "Makes the vacancy visible in search results. Republishes VacancyIndexEvent.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Published"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the vacancy owner"),
            @ApiResponse(responseCode = "404", description = "Vacancy not found")
    })
    @PatchMapping("/{id}/publish")
    public void publish(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        service.updatePublishStatus(id, userId, true);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Unpublish a vacancy", description = "Hides the vacancy from public search.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unpublished"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the vacancy owner"),
            @ApiResponse(responseCode = "404", description = "Vacancy not found")
    })
    @PatchMapping("/{id}/unpublish")
    public void unpublish(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        service.updatePublishStatus(id, userId, false);
    }

    @Operation(summary = "Get vacancies by employer (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of employer's vacancies")
    @GetMapping("/employer/{userId}")
    public Page<VacancyDto> getByEmployer(
            Pageable pageable,
            @Parameter(description = "Employer user ID", required = true) @PathVariable Long userId) {
        return service.getByEmployerId(userId, pageable).map(vacancyMapper::toDto);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a vacancy", description = "Only EMPLOYER role is allowed (enforced by Gateway). Only the vacancy owner may delete it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the vacancy owner"),
            @ApiResponse(responseCode = "404", description = "Vacancy not found")
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        service.delete(id, userId);
    }

    @Operation(summary = "Get skill IDs attached to a vacancy")
    @ApiResponse(responseCode = "200", description = "Set of skill IDs")
    @GetMapping("/{id}/skills")
    public Set<Long> getSkillIds(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id) {
        return service.getSkillIds(id);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add a skill to a vacancy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skill added"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the vacancy owner"),
            @ApiResponse(responseCode = "404", description = "Vacancy or skill not found")
    })
    @PostMapping("/{id}/skills/{skillId}")
    public void addSkill(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id,
            @Parameter(description = "Skill ID", required = true) @PathVariable Long skillId,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        service.addSkill(id, skillId, userId);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove a skill from a vacancy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skill removed"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the vacancy owner")
    })
    @DeleteMapping("/{id}/skills/{skillId}")
    public void removeSkill(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id,
            @Parameter(description = "Skill ID", required = true) @PathVariable Long skillId,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        service.removeSkill(id, skillId, userId);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Replace all skills on a vacancy", description = "Replaces the current skill set with the provided IDs. Re-indexes the vacancy in Elasticsearch.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Skills updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the vacancy owner")
    })
    @PutMapping("/{id}/skills")
    public void updateSkills(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id,
            @RequestBody Set<Long> skillIds,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        service.updateSkills(id, skillIds, userId);
    }

    @Operation(summary = "Get company name by vacancy ID")
    @ApiResponse(responseCode = "200", description = "Company name string")
    @GetMapping("/{id}/company-name")
    public String getCompanyName(
            @Parameter(description = "Vacancy ID", required = true) @PathVariable Long id) {
        var company = service.findById(id).getCompany();
        return company != null ? company.getName() : "";
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Bulk publish vacancies", description = "Publish multiple vacancies at once. Only the owner's vacancies are published.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacancies published"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the vacancy owner"),
            @ApiResponse(responseCode = "404", description = "One or more vacancies not found")
    })
    @PostMapping("/bulk-publish")
    public void bulkPublish(
            @RequestBody List<Long> ids,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        service.bulkPublish(ids, userId);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Bulk unpublish vacancies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vacancies unpublished"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the vacancy owner"),
            @ApiResponse(responseCode = "404", description = "One or more vacancies not found")
    })
    @PostMapping("/bulk-unpublish")
    public void bulkUnpublish(
            @RequestBody List<Long> ids,
            @Parameter(description = "Authenticated user ID (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        service.bulkUnpublish(ids, userId);
    }

    @Operation(summary = "Reindex all published vacancies", description = "Pushes all published vacancies back to Elasticsearch. Admin / maintenance operation.")
    @ApiResponse(responseCode = "200", description = "Reindex triggered")
    @PostMapping("/reindex")
    public void reindex() {
        service.reindexAll();
    }
}
