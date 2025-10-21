package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import com.vlz.ladorexchange_vacancyservice.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private final VacancyRepository repository;

    @Transactional(readOnly = true)
    public List<Vacancy> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Vacancy getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public Vacancy create(Vacancy vacancy) {
        return repository.save(vacancy);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
