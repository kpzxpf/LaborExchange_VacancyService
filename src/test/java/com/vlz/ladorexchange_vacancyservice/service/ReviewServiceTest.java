package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.dto.ReviewDto;
import com.vlz.ladorexchange_vacancyservice.entity.Review;
import com.vlz.ladorexchange_vacancyservice.exception.InsufficientPermissionsException;
import com.vlz.ladorexchange_vacancyservice.mapper.ReviewMapper;
import com.vlz.ladorexchange_vacancyservice.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository repository;
    @Mock ReviewMapper mapper;
    @Mock CompanyService companyService;
    @InjectMocks ReviewService service;

    private Review review;
    private ReviewDto dto;

    @BeforeEach
    void setUp() {
        review = Review.builder().id(1L).companyId(10L).authorId(100L).rating((short) 4).title("Great").text("Very good company").build();
        dto = ReviewDto.builder().rating((short) 4).title("Great").text("Very good company").build();
    }

    @Test
    void create_successfullyCreatesReview() {
        when(repository.existsByCompanyIdAndAuthorId(10L, 100L)).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(review);
        when(repository.save(review)).thenReturn(review);
        when(mapper.toDto(review)).thenReturn(dto);
        ReviewDto result = service.create(10L, 100L, dto);
        assertThat(result).isNotNull();
        verify(companyService).getById(10L);
    }

    @Test
    void create_throwsConflictIfAlreadyReviewed() {
        when(repository.existsByCompanyIdAndAuthorId(10L, 100L)).thenReturn(true);
        assertThatThrownBy(() -> service.create(10L, 100L, dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void delete_allowsAuthorToDelete() {
        when(repository.findById(1L)).thenReturn(Optional.of(review));
        assertThatCode(() -> service.delete(1L, 100L, "JOB_SEEKER")).doesNotThrowAnyException();
        verify(repository).deleteById(1L);
    }

    @Test
    void delete_allowsAdminToDelete() {
        when(repository.findById(1L)).thenReturn(Optional.of(review));
        assertThatCode(() -> service.delete(1L, 999L, "ADMIN")).doesNotThrowAnyException();
        verify(repository).deleteById(1L);
    }

    @Test
    void delete_throwsIfNotAuthorOrAdmin() {
        when(repository.findById(1L)).thenReturn(Optional.of(review));
        assertThatThrownBy(() -> service.delete(1L, 999L, "JOB_SEEKER"))
                .isInstanceOf(InsufficientPermissionsException.class);
    }

    @Test
    void update_throwsIfNotAuthor() {
        when(repository.findById(1L)).thenReturn(Optional.of(review));
        assertThatThrownBy(() -> service.update(1L, 999L, dto))
                .isInstanceOf(InsufficientPermissionsException.class);
    }

    @Test
    void findById_throwsIfNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(999L, 100L, "JOB_SEEKER"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
