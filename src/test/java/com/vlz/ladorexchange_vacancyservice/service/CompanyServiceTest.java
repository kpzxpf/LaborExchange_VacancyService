package com.vlz.ladorexchange_vacancyservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vlz.ladorexchange_vacancyservice.dto.CompanyDto;
import com.vlz.ladorexchange_vacancyservice.entity.Company;
import com.vlz.ladorexchange_vacancyservice.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository repository;

    @InjectMocks
    private CompanyService companyService;

    private final Long COMPANY_ID = 1L;
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
                .name(COMPANY_NAME)
                .email("info@tech.com")
                .location("New York")
                .build();

        when(repository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company result = companyService.create(dto);

        assertEquals(COMPANY_NAME, result.getName());
        assertEquals("info@tech.com", result.getEmail());
        assertEquals("New York", result.getLocation());
        verify(repository).save(any(Company.class));
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
    @DisplayName("delete: успех")
    void delete_Success() {
        when(repository.existsById(COMPANY_ID)).thenReturn(true);

        assertDoesNotThrow(() -> companyService.delete(COMPANY_ID));
        verify(repository).deleteById(COMPANY_ID);
    }

    @Test
    @DisplayName("delete: ошибка если компании не существует")
    void delete_NotFound_ThrowsException() {
        when(repository.existsById(COMPANY_ID)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> companyService.delete(COMPANY_ID));
        verify(repository, never()).deleteById(anyLong());
    }
}