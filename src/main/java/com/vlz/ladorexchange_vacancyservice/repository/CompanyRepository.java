package com.vlz.ladorexchange_vacancyservice.repository;

import com.vlz.ladorexchange_vacancyservice.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
