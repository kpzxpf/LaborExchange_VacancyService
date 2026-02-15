package com.vlz.ladorexchange_vacancyservice.controller;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import com.vlz.ladorexchange_vacancyservice.mapper.VacancyMapper;
import com.vlz.ladorexchange_vacancyservice.service.VacancyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService service;
    private final VacancyMapper vacancyMapper;

    @GetMapping
    public Page<VacancyDto> getAll(Pageable pageable) {
        Page<Vacancy> vacancyPage = service.getAll(pageable);

        return vacancyPage.map(vacancyMapper::toDto);
    }

    @GetMapping("/{id}")
    public VacancyDto getById(@PathVariable Long id) {
        return vacancyMapper.toDto(service.getById(id));
    }

    @PostMapping
    public VacancyDto create(
            @RequestBody @Valid VacancyDto vacancyDto,
            @RequestHeader("X-User-Id") Long userId) {
        vacancyDto.setEmployerId(userId);

        return vacancyMapper.toDto(service.create(vacancyDto));
    }

    @PostMapping("/update")
    public VacancyDto update(@RequestBody @Valid VacancyDto vacancyDto,
                             @RequestHeader("X-User-Id") Long userId) {
        return vacancyMapper.toDto(service.update(vacancyDto, userId));
    }

    @PatchMapping("/{id}/publish")
    public void publish(@PathVariable Long id,
                        @RequestHeader("X-User-Id") Long userId) {
        service.updatePublishStatus(id, userId, true);
    }

    @PatchMapping("/{id}/unpublish")
    public void unpublish(@PathVariable Long id,
                          @RequestHeader("X-User-Id") Long userId) {
        service.updatePublishStatus(id, userId, false);
    }

    @GetMapping("/employer/{userId}")
    public Page<VacancyDto> getByEmployer(Pageable pageable, @PathVariable Long userId) {
        return service.getByEmployerId(userId, pageable)
                .map(vacancyMapper::toDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}