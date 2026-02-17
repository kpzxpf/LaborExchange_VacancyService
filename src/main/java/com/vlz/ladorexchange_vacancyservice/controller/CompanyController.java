package com.vlz.ladorexchange_vacancyservice.controller;

import com.vlz.ladorexchange_vacancyservice.dto.CompanyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Company;
import com.vlz.ladorexchange_vacancyservice.mapper.CompanyMapper;
import com.vlz.ladorexchange_vacancyservice.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService service;
    private final CompanyMapper companyMapper;

    @GetMapping
    public List<CompanyDto> getAll() {
        return companyMapper.toDtoList(service.getAll());
    }

    @GetMapping("/{id}")
    public CompanyDto getById(@PathVariable Long id) {
        return companyMapper.toDto(service.getById(id));
    }

    @PostMapping
    public CompanyDto create(@RequestBody @Valid CompanyDto dto) {
        Company company = service.create(dto);
        return companyMapper.toDto(company);
    }

    @GetMapping("/{id}/company")
    public String getCompanyName(@PathVariable("id") Long id) {
        return service.getCompanyNameByVacancyId(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
