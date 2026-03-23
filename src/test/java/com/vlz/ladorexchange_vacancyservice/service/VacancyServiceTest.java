package com.vlz.ladorexchange_vacancyservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vlz.ladorexchange_vacancyservice.dto.VacancyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Company;
import com.vlz.ladorexchange_vacancyservice.entity.Vacancy;
import com.vlz.ladorexchange_vacancyservice.exception.InsufficientPermissionsException;
import com.vlz.ladorexchange_vacancyservice.service.ContentModerationService;
import com.vlz.ladorexchange_vacancyservice.mapper.VacancyMapper;
import com.vlz.ladorexchange_vacancyservice.producer.VacancyIndexProducer;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class VacancyServiceTest {

    @Mock
    private VacancyRepository repository;

    @Mock
    private CompanyService companyService;

    @Mock
    private RoleRetryClient roleRetryClient;

    @Mock
    private VacancyIndexProducer vacancyIndexProducer;

    @Mock
    private SkillRetryClient skillRetryClient;

    @Mock
    private VacancyMapper vacancyMapper;

    @Mock
    private ContentModerationService contentModerationService;

    @InjectMocks
    private VacancyService vacancyService;

    private final String REQUIRED_ROLE = "EMPLOYER";
    private final Long USER_ID = 10L;
    private final Long VACANCY_ID = 1L;

    private final Company stubCompany = Company.builder()
            .id(1L).name("Best Company").location("Moscow").build();

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
            when(companyService.findOrCreateByName("Best Company", USER_ID)).thenReturn(stubCompany);
            when(repository.save(any(Vacancy.class))).thenAnswer(i -> {
                Vacancy v = i.getArgument(0);
                return Vacancy.builder().id(1L).title(v.getTitle()).employerId(v.getEmployerId()).company(v.getCompany()).build();
            });

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
            VacancyDto updateDto = VacancyDto.builder()
                    .id(VACANCY_ID).title("New Title").employerId(USER_ID).companyName("Best Company").build();

            when(repository.findById(VACANCY_ID)).thenReturn(Optional.of(existing));
            when(companyService.findOrCreateByName("Best Company", USER_ID)).thenReturn(stubCompany);
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

    @Nested
    @DisplayName("Метод updatePublishStatus")
    class UpdatePublishStatusTests {

        @Test
        @DisplayName("Успех: публикация вакансии отправляет Kafka-событие")
        void updatePublishStatus_Publish_SendsIndexEvent() {
            Vacancy vacancy = Vacancy.builder()
                    .id(VACANCY_ID).employerId(USER_ID).company(stubCompany)
                    .skillIds(Set.of()).build();

            when(repository.findById(VACANCY_ID)).thenReturn(Optional.of(vacancy));
            when(repository.save(any())).thenReturn(vacancy);
            when(skillRetryClient.getNameSkillsByIds(anyList())).thenReturn(Set.of());

            vacancyService.updatePublishStatus(VACANCY_ID, USER_ID, true);

            verify(vacancyIndexProducer, times(1)).send(any());
        }

        @Test
        @DisplayName("Успех: снятие с публикации не отправляет Kafka-событие")
        void updatePublishStatus_Unpublish_NoIndexEvent() {
            Vacancy vacancy = Vacancy.builder()
                    .id(VACANCY_ID).employerId(USER_ID).company(stubCompany).build();

            when(repository.findById(VACANCY_ID)).thenReturn(Optional.of(vacancy));
            when(repository.save(any())).thenReturn(vacancy);

            vacancyService.updatePublishStatus(VACANCY_ID, USER_ID, false);

            verify(vacancyIndexProducer, never()).send(any());
        }

        @Test
        @DisplayName("Ошибка: чужой пользователь не может изменить статус публикации")
        void updatePublishStatus_NotOwner_ThrowsException() {
            Vacancy vacancy = Vacancy.builder()
                    .id(VACANCY_ID).employerId(USER_ID).build();

            when(repository.findById(VACANCY_ID)).thenReturn(Optional.of(vacancy));

            assertThrows(InsufficientPermissionsException.class,
                    () -> vacancyService.updatePublishStatus(VACANCY_ID, 999L, true));
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Метод bulkPublish / bulkUnpublish")
    class BulkPublishTests {

        @Test
        @DisplayName("bulkPublish: все вакансии опубликованы, по одному Kafka-событию на каждую")
        void bulkPublish_Success_SendsIndexEvents() {
            Vacancy v1 = Vacancy.builder().id(1L).employerId(USER_ID).company(stubCompany).skillIds(Set.of()).build();
            Vacancy v2 = Vacancy.builder().id(2L).employerId(USER_ID).company(stubCompany).skillIds(Set.of()).build();

            when(repository.findById(1L)).thenReturn(Optional.of(v1));
            when(repository.findById(2L)).thenReturn(Optional.of(v2));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(skillRetryClient.getNameSkillsByIds(anyList())).thenReturn(Set.of());

            vacancyService.bulkPublish(List.of(1L, 2L), USER_ID);

            verify(vacancyIndexProducer, times(2)).send(any());
        }

        @Test
        @DisplayName("bulkPublish: ошибка при попытке опубликовать чужую вакансию")
        void bulkPublish_NotOwner_ThrowsException() {
            Vacancy v = Vacancy.builder().id(1L).employerId(USER_ID).build();
            when(repository.findById(1L)).thenReturn(Optional.of(v));

            assertThrows(InsufficientPermissionsException.class,
                    () -> vacancyService.bulkPublish(List.of(1L), 999L));
        }

        @Test
        @DisplayName("bulkUnpublish: все вакансии сняты, отправляются deleted=true события")
        void bulkUnpublish_Success_SendsDeletedEvents() {
            Vacancy v1 = Vacancy.builder().id(1L).employerId(USER_ID).build();
            Vacancy v2 = Vacancy.builder().id(2L).employerId(USER_ID).build();

            when(repository.findById(1L)).thenReturn(Optional.of(v1));
            when(repository.findById(2L)).thenReturn(Optional.of(v2));
            when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

            vacancyService.bulkUnpublish(List.of(1L, 2L), USER_ID);

            verify(vacancyIndexProducer, times(2)).send(argThat(e -> e.isDeleted()));
        }

        @Test
        @DisplayName("bulkUnpublish: ошибка при попытке снять чужую вакансию")
        void bulkUnpublish_NotOwner_ThrowsException() {
            Vacancy v = Vacancy.builder().id(1L).employerId(USER_ID).build();
            when(repository.findById(1L)).thenReturn(Optional.of(v));

            assertThrows(InsufficientPermissionsException.class,
                    () -> vacancyService.bulkUnpublish(List.of(1L), 999L));
        }
    }

    @Nested
    @DisplayName("Метод reindexAll")
    class ReindexAllTests {

        @Test
        @DisplayName("Успех: один вызов skillRetryClient для всех вакансий (батч)")
        void reindexAll_UsesSingleBatchCallToSkillService() {
            Vacancy v1 = Vacancy.builder()
                    .id(1L).title("Dev").company(stubCompany)
                    .skillIds(Set.of(10L, 20L)).build();
            Vacancy v2 = Vacancy.builder()
                    .id(2L).title("QA").company(stubCompany)
                    .skillIds(Set.of(20L, 30L)).build();

            when(repository.findAllByIsPublishedTrue()).thenReturn(List.of(v1, v2));
            when(skillRetryClient.getSkillMapByIds(anyList()))
                    .thenReturn(Map.of(10L, "Java", 20L, "Spring", 30L, "Testing"));

            vacancyService.reindexAll();

            // Only ONE call to skillRetryClient regardless of number of vacancies
            verify(skillRetryClient, times(1)).getSkillMapByIds(anyList());
            // One indexing event per vacancy
            verify(vacancyIndexProducer, times(2)).send(any());
        }

        @Test
        @DisplayName("Пустой список вакансий: skillRetryClient не вызывается")
        void reindexAll_NoVacancies_NoSkillServiceCall() {
            when(repository.findAllByIsPublishedTrue()).thenReturn(Collections.emptyList());

            vacancyService.reindexAll();

            verify(skillRetryClient, never()).getSkillMapByIds(any());
            verify(vacancyIndexProducer, never()).send(any());
        }

        @Test
        @DisplayName("Вакансии без навыков: skillRetryClient не вызывается")
        void reindexAll_VacanciesWithNoSkills_NoSkillServiceCall() {
            Vacancy v = Vacancy.builder()
                    .id(1L).title("No Skills").company(stubCompany)
                    .skillIds(null).build();

            when(repository.findAllByIsPublishedTrue()).thenReturn(List.of(v));

            vacancyService.reindexAll();

            verify(skillRetryClient, never()).getSkillMapByIds(any());
            verify(vacancyIndexProducer, times(1)).send(any());
        }
    }
}
