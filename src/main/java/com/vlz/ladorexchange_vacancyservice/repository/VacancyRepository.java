package com.vlz.ladorexchange_vacancyservice.repository;

import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    Page<Vacancy> findAllByIsPublishedTrue(Pageable pageable);
    Page<Vacancy> findAllByEmployerId(Long employerId, Pageable pageable);
    List<Vacancy> findAllByEmployerId(Long employerId);
    List<Vacancy> findAllByIsPublishedTrue();
}
