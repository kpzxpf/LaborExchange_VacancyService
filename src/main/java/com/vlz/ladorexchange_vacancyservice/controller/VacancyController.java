package com.vlz.ladorexchange_vacancyservice.controller;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import com.vlz.ladorexchange_vacancyservice.mapper.VacancyMapper;
import com.vlz.ladorexchange_vacancyservice.service.VacancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService service;
    private final VacancyMapper vacancyMapper;

    @GetMapping
    public List<VacancyDto> getAll() {
        return vacancyMapper.toDtoList(service.getAll());
    }

    @GetMapping("/{id}")
    public VacancyDto getById(@PathVariable Long id) {
        return vacancyMapper.toDto(service.getById(id));
    }

    @PostMapping
    public VacancyDto create(@RequestBody Vacancy vacancy) {
        return vacancyMapper.toDto(service.create(vacancy));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}