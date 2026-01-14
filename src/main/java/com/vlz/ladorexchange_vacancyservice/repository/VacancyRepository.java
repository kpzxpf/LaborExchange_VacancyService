package com.vlz.ladorexchange_vacancyservice.repository;

import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

}
