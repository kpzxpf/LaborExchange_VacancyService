package com.vlz.ladorexchange_vacancyservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vlz.ladorexchange_vacancyservice.dto.CompanyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Company;
import com.vlz.ladorexchange_vacancyservice.exception.InsufficientPermissionsException;
import com.vlz.ladorexchange_vacancyservice.mapper.CompanyMapper;
import com.vlz.ladorexchange_vacancyservice.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository repository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    private final Long COMPANY_ID = 1L;
    private final Long EMPLOYER_ID = 42L;
    private final Long OTHER_USER_ID = 99L;
    private final String COMPANY_NAME = "Tech Corp";

    @Test
    @DisplayName("getById: успех — возвращает компанию")
    void getById_Success() {
        Company company = Company.builder().id(COMPANY_ID).name(COMPANY_NAME).build();
        when(repository.findById(COMPANY_ID)).thenReturn(Optional.of(company));

        Company result = companyService.getById(COMPANY_ID);

        assertNotNull(result);
        assertEquals(COMPANY_NAME, result.getName());
        verify(repository).findById(COMPANY_ID);
    }

    @Test
    @DisplayName("getById: ошибка — компания не найдена")
    void getById_NotFound_ThrowsException() {
        when(repository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> companyService.getById(COMPANY_ID));
    }

    @Test
    @DisplayName("create: проверка корректного маппинга полей из DTO")
    void create_Success() {
        CompanyDto dto = CompanyDto.builder()
                .employerId(EMPLOYER_ID)
                .name(COMPANY_NAME)
                .email("info@tech.com")
                .location("New York")
                .build();

        when(repository.findByEmployerId(EMPLOYER_ID)).thenReturn(Optional.empty());
        when(repository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company result = companyService.create(dto);

        assertEquals(COMPANY_NAME, result.getName());
        assertEquals("info@tech.com", result.getEmail());
        assertEquals("New York", result.getLocation());
        verify(repository).save(any(Company.class));
    }

    @Test
    @DisplayName("create: ошибка 409 — у работодателя уже есть компания")
    void create_DuplicateEmployer_ThrowsConflict() {
        CompanyDto dto = CompanyDto.builder()
                .employerId(EMPLOYER_ID)
                .name("Another Corp")
                .build();

        Company existing = Company.builder().id(COMPANY_ID).employerId(EMPLOYER_ID).name(COMPANY_NAME).build();
        when(repository.findByEmployerId(EMPLOYER_ID)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () -> companyService.create(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("getByName: успех")
    void getByName_Success() {
        Company company = Company.builder().name(COMPANY_NAME).build();
        when(repository.findByName(COMPANY_NAME)).thenReturn(Optional.of(company));

        Company result = companyService.getByName(COMPANY_NAME);

        assertEquals(COMPANY_NAME, result.getName());
    }

    @Test
    @DisplayName("delete: успех — владелец удаляет свою компанию")
    void delete_Success() {
        Company company = Company.builder().id(COMPANY_ID).employerId(EMPLOYER_ID).name(COMPANY_NAME).build();
        when(repository.findById(COMPANY_ID)).thenReturn(Optional.of(company));

        assertDoesNotThrow(() -> companyService.delete(COMPANY_ID, EMPLOYER_ID));
        verify(repository).deleteById(COMPANY_ID);
    }

    @Test
    @DisplayName("delete: ошибка если компании не существует")
    void delete_NotFound_ThrowsException() {
        when(repository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> companyService.delete(COMPANY_ID, EMPLOYER_ID));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete: ошибка 403 — чужой пользователь не может удалить компанию")
    void delete_NotOwner_ThrowsForbidden() {
        Company company = Company.builder().id(COMPANY_ID).employerId(EMPLOYER_ID).name(COMPANY_NAME).build();
        when(repository.findById(COMPANY_ID)).thenReturn(Optional.of(company));

        assertThrows(InsufficientPermissionsException.class,
                () -> companyService.delete(COMPANY_ID, OTHER_USER_ID));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("verify: успех — устанавливает isVerified=true и возвращает DTO")
    void verify_Success() {
        Company company = Company.builder().id(COMPANY_ID).name(COMPANY_NAME).isVerified(false).build();
        CompanyDto expected = CompanyDto.builder().id(COMPANY_ID).name(COMPANY_NAME).build();

        when(repository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(repository.save(any(Company.class))).thenAnswer(i -> i.getArgument(0));
        when(companyMapper.toDto(any(Company.class))).thenReturn(expected);

        CompanyDto result = companyService.verify(COMPANY_ID);

        assertNotNull(result);
        assertTrue(company.isVerified());
        verify(repository).save(company);
    }

    @Test
    @DisplayName("verify: ошибка — компания не найдена")
    void verify_NotFound_ThrowsException() {
        when(repository.findById(COMPANY_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> companyService.verify(COMPANY_ID));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("unverify: успех — сбрасывает isVerified=false")
    void unverify_Success() {
        Company company = Company.builder().id(COMPANY_ID).name(COMPANY_NAME).isVerified(true).build();
        CompanyDto expected = CompanyDto.builder().id(COMPANY_ID).name(COMPANY_NAME).build();

        when(repository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
        when(repository.save(any(Company.class))).thenAnswer(i -> i.getArgument(0));
        when(companyMapper.toDto(any(Company.class))).thenReturn(expected);

        companyService.unverify(COMPANY_ID);

        assertFalse(company.isVerified());
        verify(repository).save(company);
    }
}