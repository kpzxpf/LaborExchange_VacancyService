package com.vlz.ladorexchange_vacancyservice.mapper;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VacancyMapper {

    VacancyDto toDto(Vacancy vacancy);

    Vacancy toEntity(VacancyDto dto);

    List<VacancyDto> toDtoList(List<Vacancy> vacancies);

    List<Vacancy> toEntityList(List<VacancyDto> dtos);
}
