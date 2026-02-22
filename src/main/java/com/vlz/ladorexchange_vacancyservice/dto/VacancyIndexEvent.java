package com.vlz.ladorexchange_vacancyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyIndexEvent {
    private long id;
    private String title;
    private String description;
    private String companyName;
    private String location;
    private Set<String> skills;
}
