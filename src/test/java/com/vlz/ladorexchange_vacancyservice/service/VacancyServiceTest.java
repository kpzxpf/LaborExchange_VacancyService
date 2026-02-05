package com.vlz.ladorexchange_vacancyservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import com.vlz.ladorexchange_vacancyservice.exception.InsufficientPermissionsException;
import com.vlz.ladorexchange_vacancyservice.repository.VacancyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class VacancyServiceTest {

    @Mock
    private VacancyRepository repository;

    @Mock
    private CompanyService companyService;

    @Mock
    private RoleRetryClient roleRetryClient;

    @InjectMocks
    private VacancyService vacancyService;

    private final String REQUIRED_ROLE = "EMPLOYER";
    private final Long USER_ID = 10L;
    private final Long VACANCY_ID = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vacancyService, "needRoleForCreate", REQUIRED_ROLE);
    }

    @Nested
    @DisplayName("Метод create")
    class CreateTests {
        @Test
        @DisplayName("Успех: роль совпадает, вакансия сохраняется")
        void create_Success() {
            VacancyDto dto = VacancyDto.builder()
                    .title("Java Developer")
                    .employerId(USER_ID)
                    .companyName("Best Company")
                    .build();

            when(roleRetryClient.getUserRoleById(USER_ID)).thenReturn(REQUIRED_ROLE);
            when(repository.save(any(Vacancy.class))).thenAnswer(i -> i.getArgument(0));

            Vacancy result = vacancyService.create(dto);

            assertNotNull(result);
            assertEquals("Java Developer", result.getTitle());
            verify(repository).save(any(Vacancy.class));
        }

        @Test
        @DisplayName("Ошибка: роль не совпадает, выбрасывается исключение")
        void create_WrongRole_ThrowsException() {
            VacancyDto dto = VacancyDto.builder().employerId(USER_ID).build();
            when(roleRetryClient.getUserRoleById(USER_ID)).thenReturn("USER");

            assertThrows(InsufficientPermissionsException.class, () -> vacancyService.create(dto));
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Метод update и проверка владения")
    class UpdateTests {
        @Test
        @DisplayName("Успех: владелец может обновить свою вакансию")
        void update_Success() {
            Vacancy existing = Vacancy.builder().id(VACANCY_ID).employerId(USER_ID).build();
            VacancyDto updateDto = VacancyDto.builder().id(VACANCY_ID).title("New Title").employerId(USER_ID).build();

            when(repository.findById(VACANCY_ID)).thenReturn(Optional.of(existing));
            when(repository.save(any(Vacancy.class))).thenAnswer(i -> i.getArgument(0));

            Vacancy result = vacancyService.update(updateDto, USER_ID);

            assertEquals("New Title", result.getTitle());
        }

        @Test
        @DisplayName("Ошибка: попытка обновить чужую вакансию")
        void update_NotOwner_ThrowsException() {
            Vacancy existing = Vacancy.builder().id(VACANCY_ID).employerId(USER_ID).build();
            VacancyDto updateDto = VacancyDto.builder().id(VACANCY_ID).employerId(999L).build();

            when(repository.findById(VACANCY_ID)).thenReturn(Optional.of(existing));

            assertThrows(InsufficientPermissionsException.class, () -> vacancyService.update(updateDto, 999L));
        }
    }
}