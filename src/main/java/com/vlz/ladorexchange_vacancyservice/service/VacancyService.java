package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.dto.exception.InsufficientPermissionsException;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import com.vlz.ladorexchange_vacancyservice.repository.VacancyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacancyService {

    private final VacancyRepository repository;
    private final CompanyService companyService;
    private final RoleRetryClient roleRetryClient;

    @Value("${spring.vacancy-create.role}")
    private String needRoleForCreate;

    @Transactional(readOnly = true)
    public Page<Vacancy> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Vacancy getById(Long id) {
        return repository.findById(id).orElseThrow(() -> {
            log.error("id not found {}", id);
            return new EntityNotFoundException("id not found " + id);
        });
    }

    @Transactional
    public Vacancy create(VacancyDto vacancyDto) {
        String userRole = roleRetryClient.getUserRoleById(vacancyDto.getEmployerId());

        if (!needRoleForCreate.equals(userRole)) {
            log.error("User {} tried to create a new Vacancy", userRole);
            throw new InsufficientPermissionsException(
                    "Only users with EMPLOYER role can create vacancies. Current role: " + userRole
            );
        }

        Vacancy vacancy = Vacancy.builder()
                .title(vacancyDto.getTitle())
                .description(vacancyDto.getDescription())
                .salary(vacancyDto.getSalary())
                .employerId(vacancyDto.getEmployerId())
                .company(companyService.getById(vacancyDto.getId()))
                .build();

        return repository.save(vacancy);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
