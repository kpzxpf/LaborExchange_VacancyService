package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.dto.CompanyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Company;
import com.vlz.ladorexchange_vacancyservice.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository repository;

    @Transactional(readOnly = true)
    public List<Company> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Company getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Company id {} not found", id);
                    return new EntityNotFoundException("Company id " + id);
                });
    }

    @Transactional
    public Company create(CompanyDto dto) {
        Company company = Company.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .website(dto.getWebsite())
                .build();

        return repository.save(company);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            log.error("Company id {} not found", id);
            throw new EntityNotFoundException("Company not found");
        }
        repository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public Company getByName(String companyName) {
        return repository.findByName(companyName).orElseThrow(() -> {
            log.error("Company name {} not found", companyName);
            return new EntityNotFoundException("Company name " + companyName);
        });
    }

    @Transactional(readOnly = true)
    public String getCompanyNameByVacancyId(Long vacancyId) {
        return repository.findNameByVacancyId(vacancyId);
    }
}