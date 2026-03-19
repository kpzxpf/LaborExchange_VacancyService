package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.dto.CompanyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Company;
import com.vlz.ladorexchange_vacancyservice.exception.InsufficientPermissionsException;
import com.vlz.ladorexchange_vacancyservice.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
                .employerId(dto.getEmployerId())
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
    public Company update(Long id, Long userId, CompanyDto dto) {
        Company company = getById(id);
        if (!company.getEmployerId().equals(userId)) {
            throw new InsufficientPermissionsException("You are not the owner of this company");
        }
        company.setName(dto.getName());
        company.setDescription(dto.getDescription());
        company.setLocation(dto.getLocation());
        company.setEmail(dto.getEmail());
        company.setPhoneNumber(dto.getPhoneNumber());
        company.setWebsite(dto.getWebsite());
        return repository.save(company);
    }

    @Transactional(readOnly = true)
    public Optional<Company> findByEmployerId(Long employerId) {
        return repository.findByEmployerId(employerId);
    }

    @Transactional(readOnly = true)
    public Company getByEmployerId(Long employerId) {
        return repository.findByEmployerId(employerId)
                .orElseThrow(() -> {
                    log.error("Company not found for employer {}", employerId);
                    return new EntityNotFoundException("Company not found for employer " + employerId);
                });
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

    @Transactional
    public Company findOrCreateByName(String companyName, Long employerId) {
        Optional<Company> byName = repository.findByName(companyName);
        if (byName.isPresent()) {
            return byName.get();
        }
        Optional<Company> byEmployer = repository.findByEmployerId(employerId);
        if (byEmployer.isPresent()) {
            log.warn("Company name '{}' not found, using employer's existing company '{}'",
                    companyName, byEmployer.get().getName());
            return byEmployer.get();
        }
        log.error("Company '{}' not found and employer {} has no registered company", companyName, employerId);
        throw new EntityNotFoundException(
                "Company '" + companyName + "' not found. Please create your company first.");
    }

    @Transactional(readOnly = true)
    public String getCompanyNameByVacancyId(Long vacancyId) {
        return repository.findNameByVacancyId(vacancyId);
    }
}