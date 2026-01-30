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
    public VacancyDto create(@RequestBody @Valid VacancyDto vacancyDto) {
        return vacancyMapper.toDto(service.create(vacancyDto));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}