package com.vlz.ladorexchange_vacancyservice.repository;

import com.vlz.ladorexchange_vacancyservice.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String companyName);

    @Query("SELECT c.name FROM Company c JOIN Vacancy v ON v.company.id = c.id WHERE v.id = :vacancyId")
    String findNameByVacancyId(@Param("vacancyId") Long vacancyId);
}
