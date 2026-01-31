package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.exception.InsufficientPermissionsException;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import com.vlz.ladorexchange_vacancyservice.repository.VacancyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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
        return repository.findAllByIsPublishedTrue(pageable);
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
        checkForRequiredRole(vacancyDto.getEmployerId());

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
    public Vacancy update(@Valid VacancyDto vacancyDto, Long userId) {
        Vacancy vacancy = getById(vacancyDto.getId());

        validateOwnership(vacancy.getEmployerId(), userId);

        vacancy.setTitle(vacancyDto.getTitle());
        vacancy.setDescription(vacancyDto.getDescription());
        vacancy.setSalary(vacancyDto.getSalary());
        vacancy.setEmployerId(vacancyDto.getEmployerId());
        vacancy.setCompany(companyService.getById(vacancyDto.getId()));

        return repository.save(vacancy);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void updatePublishStatus(Long id, Long userId, boolean status) {
        Vacancy vacancy = getById(id);

        validateOwnership(vacancy.getEmployerId(), userId);

        if (!vacancy.getIsPublished()) {
            log.error("id {} not published", id);
            throw new InsufficientPermissionsException("This vacancy is private");
        }
        vacancy.setIsPublished(status);
    }

    private void checkForRequiredRole(Long userId) {
        String userRole = roleRetryClient.getUserRoleById(userId);

        if (!needRoleForCreate.equals(userRole)) {
            log.error("User {} tried to create a new Vacancy", userRole);
            throw new InsufficientPermissionsException(
                    "Only users with EMPLOYER role can create vacancies. Current role: " + userRole
            );
        }
    }

    private void validateOwnership(Long vacancyUserId, Long userId) {
        if (!vacancyUserId.equals(userId)) {
            log.error("Access denied: User {} is not owner of vacancy", userId);
            throw new InsufficientPermissionsException("You can only edit your own vacancies");
        }
    }
}
