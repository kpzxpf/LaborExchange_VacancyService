package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.dto.VacancyIndexEvent;
import com.vlz.ladorexchange_vacancyservice.exception.InsufficientPermissionsException;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import com.vlz.ladorexchange_vacancyservice.mapper.VacancyMapper;
import com.vlz.ladorexchange_vacancyservice.producer.VacancyIndexProducer;
import com.vlz.ladorexchange_vacancyservice.repository.VacancyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacancyService {

    private final VacancyRepository repository;
    private final CompanyService companyService;
    private final RoleRetryClient roleRetryClient;
    private final VacancyIndexProducer vacancyIndexProducer;
    private final SkillRetryClient skillRetryClient;
    private final VacancyMapper vacancyMapper;

    @Value("${spring.vacancy-create.role}")
    private String needRoleForCreate;

    @Transactional(readOnly = true)
    public Page<Vacancy> getAll(Pageable pageable) {
        return repository.findAllByIsPublishedTrue(pageable);
    }

    // Internal entity fetch — no Redis cache (avoids Hibernate PersistentSet serialization issues)
    @Transactional(readOnly = true)
    public Vacancy findById(Long id) {
        return repository.findById(id).orElseThrow(() -> {
            log.error("id not found {}", id);
            return new EntityNotFoundException("id not found " + id);
        });
    }

    // Public DTO fetch — safe to cache in Redis (plain POJO, no Hibernate proxies)
    @Cacheable(value = "vacancies", key = "#id")
    @Transactional(readOnly = true)
    public VacancyDto getById(Long id) {
        return vacancyMapper.toDto(findById(id));
    }

    @Caching(evict = {
        @CacheEvict(value = "vacancies:list", allEntries = true),
        @CacheEvict(value = "vacancies:employer", allEntries = true)
    })
    @Transactional
    public Vacancy create(VacancyDto vacancyDto) {
        checkForRequiredRole(vacancyDto.getEmployerId());

        Vacancy vacancy = Vacancy.builder()
                .title(vacancyDto.getTitle())
                .description(vacancyDto.getDescription())
                .salary(vacancyDto.getSalary())
                .employerId(vacancyDto.getEmployerId())
                .company(companyService.findOrCreateByName(vacancyDto.getCompanyName(), vacancyDto.getEmployerId()))
                .isPublished(vacancyDto.isPublished())
                .build();


        Vacancy savedVacancy = repository.save(vacancy);

        vacancyIndexProducer.send(VacancyIndexEvent.builder()
                .id(savedVacancy.getId())
                .title(savedVacancy.getTitle())
                .description(savedVacancy.getDescription())
                .companyName(savedVacancy.getCompany().getName())
                .location(savedVacancy.getCompany().getLocation())
                .salary(savedVacancy.getSalary())
                .createdAt(savedVacancy.getCreatedAt())
                .skills(skillRetryClient.getNameSkillsByIds(
                        List.copyOf(savedVacancy.getSkillIds() != null ? savedVacancy.getSkillIds() : new HashSet<>())))
                .build());
        return savedVacancy;
    }

    @Caching(evict = {
        @CacheEvict(value = "vacancies", key = "#vacancyDto.id"),
        @CacheEvict(value = "vacancies:list", allEntries = true),
        @CacheEvict(value = "vacancies:employer", allEntries = true)
    })
    @Transactional
    public Vacancy update(@Valid @RequestBody VacancyDto vacancyDto, Long userId) {
        Vacancy vacancy = findById(vacancyDto.getId());

        validateOwnership(vacancy.getEmployerId(), userId);

        vacancy.setTitle(vacancyDto.getTitle());
        vacancy.setDescription(vacancyDto.getDescription());
        vacancy.setSalary(vacancyDto.getSalary());
        vacancy.setEmployerId(vacancyDto.getEmployerId());
        vacancy.setCompany(companyService.findOrCreateByName(vacancyDto.getCompanyName(), vacancyDto.getEmployerId()));

        Vacancy saved = repository.save(vacancy);

        vacancyIndexProducer.send(VacancyIndexEvent.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .companyName(saved.getCompany().getName())
                .location(saved.getCompany().getLocation())
                .salary(saved.getSalary())
                .createdAt(saved.getCreatedAt())
                .skills(skillRetryClient.getNameSkillsByIds(
                        List.copyOf(saved.getSkillIds() != null ? saved.getSkillIds() : new HashSet<>())))
                .build());

        return saved;
    }

    @Caching(evict = {
        @CacheEvict(value = "vacancies", key = "#id"),
        @CacheEvict(value = "vacancies:list", allEntries = true),
        @CacheEvict(value = "vacancies:employer", allEntries = true)
    })
    @Transactional
    public void delete(Long id, Long userId) {
        Vacancy vacancy = findById(id);
        validateOwnership(vacancy.getEmployerId(), userId);
        repository.deleteById(id);
    }

    @Caching(evict = {
        @CacheEvict(value = "vacancies", key = "#id"),
        @CacheEvict(value = "vacancies:list", allEntries = true),
        @CacheEvict(value = "vacancies:employer", allEntries = true)
    })
    @Transactional
    public void updatePublishStatus(Long id, Long userId, boolean status) {
        Vacancy vacancy = findById(id);

        validateOwnership(vacancy.getEmployerId(), userId);

        vacancy.setPublished(status);
        repository.save(vacancy);
    }

    @Transactional(readOnly = true)
    public Page<Vacancy> getByEmployerId(Long employerId, Pageable pageable) {
        return repository.findAllByEmployerId(employerId, pageable);
    }

    @Transactional(readOnly = true)
    public Set<Long> getSkillIds(Long vacancyId) {
        Vacancy vacancy = findById(vacancyId);

        return vacancy.getSkillIds() != null ? vacancy.getSkillIds() : new HashSet<>();
    }

    @Caching(evict = {
        @CacheEvict(value = "vacancies", key = "#vacancyId"),
        @CacheEvict(value = "vacancies:list", allEntries = true)
    })
    @Transactional
    public void addSkill(Long vacancyId, Long skillId, Long userId) {
        Vacancy vacancy = findById(vacancyId);
        validateOwnership(vacancy.getEmployerId(), userId);

        if (vacancy.getSkillIds() == null) {
            vacancy.setSkillIds(new HashSet<>());
        }

        vacancy.getSkillIds().add(skillId);
        repository.save(vacancy);
    }

    @Caching(evict = {
        @CacheEvict(value = "vacancies", key = "#vacancyId"),
        @CacheEvict(value = "vacancies:list", allEntries = true)
    })
    @Transactional
    public void removeSkill(Long vacancyId, Long skillId, Long userId) {
        Vacancy vacancy = findById(vacancyId);
        validateOwnership(vacancy.getEmployerId(), userId);

        if (vacancy.getSkillIds() != null) {
            vacancy.getSkillIds().remove(skillId);
        }

        repository.save(vacancy);
    }

    @Caching(evict = {
        @CacheEvict(value = "vacancies", key = "#vacancyId"),
        @CacheEvict(value = "vacancies:list", allEntries = true)
    })
    @Transactional
    public void updateSkills(Long vacancyId, Set<Long> skillIds, Long userId) {
        Vacancy vacancy = findById(vacancyId);
        validateOwnership(vacancy.getEmployerId(), userId);

        vacancy.setSkillIds(skillIds != null ? skillIds : new HashSet<>());
        Vacancy saved = repository.save(vacancy);

        vacancyIndexProducer.send(VacancyIndexEvent.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .companyName(saved.getCompany().getName())
                .location(saved.getCompany().getLocation())
                .salary(saved.getSalary())
                .createdAt(saved.getCreatedAt())
                .skills(skillRetryClient.getNameSkillsByIds(List.copyOf(saved.getSkillIds() != null ? saved.getSkillIds() : new HashSet<>())))
                .build());
    }

    @Transactional(readOnly = true)
    public void reindexAll() {
        List<Vacancy> vacancies = repository.findAllByIsPublishedTrue();
        log.info("Reindexing {} vacancies", vacancies.size());

        vacancies.forEach(vacancy -> vacancyIndexProducer.send(VacancyIndexEvent.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .description(vacancy.getDescription())
                .companyName(vacancy.getCompany().getName())
                .location(vacancy.getCompany().getLocation())
                .salary(vacancy.getSalary())
                .createdAt(vacancy.getCreatedAt())
                .skills(skillRetryClient.getNameSkillsByIds(
                        List.copyOf(vacancy.getSkillIds() != null ? vacancy.getSkillIds() : new HashSet<>())))
                .build()));
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
