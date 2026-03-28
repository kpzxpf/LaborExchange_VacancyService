package com.vlz.ladorexchange_vacancyservice.mapper;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VacancyMapper {

    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "isPublished", source = "published")
    VacancyDto toDto(Vacancy vacancy);
}
