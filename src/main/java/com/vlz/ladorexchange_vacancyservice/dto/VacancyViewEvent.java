package com.vlz.ladorexchange_vacancyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyViewEvent {
    private Long vacancyId;
    private String vacancyTitle;
    private Long employerId;
    private Long viewerId;
    private String viewerRole;
    private LocalDateTime viewedAt;
}
