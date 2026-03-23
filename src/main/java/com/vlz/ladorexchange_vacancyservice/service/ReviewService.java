package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.dto.ReviewDto;
import com.vlz.ladorexchange_vacancyservice.dto.ReviewSummaryDto;
import com.vlz.ladorexchange_vacancyservice.entity.Review;
import com.vlz.ladorexchange_vacancyservice.exception.InsufficientPermissionsException;
import com.vlz.ladorexchange_vacancyservice.mapper.ReviewMapper;
import com.vlz.ladorexchange_vacancyservice.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final CompanyService companyService;

    @Transactional(readOnly = true)
    public Page<ReviewDto> getByCompanyId(Long companyId, Pageable pageable) {
        companyService.getById(companyId);

        Double avg = repository.findAverageRatingByCompanyId(companyId).orElse(0.0);
        long total = repository.countByCompanyId(companyId);

        return repository.findAllByCompanyId(companyId, pageable)
                .map(review -> {
                    ReviewDto dto = mapper.toDto(review);
                    dto.setAverageRating(Math.round(avg * 10.0) / 10.0);
                    dto.setTotalReviews(total);
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public ReviewSummaryDto getSummary(Long companyId) {
        companyService.getById(companyId);

        Double avg = repository.findAverageRatingByCompanyId(companyId).orElse(0.0);
        long total = repository.countByCompanyId(companyId);

        List<Object[]> dist = repository.findRatingDistributionByCompanyId(companyId);
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        dist.forEach(row -> distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue()));

        return ReviewSummaryDto.builder()
                .companyId(companyId)
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalReviews(total)
                .ratingDistribution(distribution)
                .build();
    }

    @Transactional
    public ReviewDto create(Long companyId, Long authorId, ReviewDto dto) {
        companyService.getById(companyId);

        if (repository.existsByCompanyIdAndAuthorId(companyId, authorId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already left a review for this company");
        }

        Review review = mapper.toEntity(dto);
        review.setCompanyId(companyId);
        review.setAuthorId(authorId);

        try {
            ReviewDto result = mapper.toDto(repository.save(review));
            log.info("Review created: companyId={} authorId={}", companyId, authorId);
            return result;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already left a review for this company");
        }
    }

    @Transactional
    public ReviewDto update(Long reviewId, Long authorId, ReviewDto dto) {
        Review review = findById(reviewId);

        if (!review.getAuthorId().equals(authorId)) {
            throw new InsufficientPermissionsException("You can only edit your own reviews");
        }

        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setText(dto.getText());

        ReviewDto result = mapper.toDto(repository.save(review));

        log.info("Review updated: id={} authorId={}", reviewId, authorId);

        return result;
    }

    @Transactional
    public void delete(Long reviewId, Long requesterId, String requesterRole) {
        Review review = findById(reviewId);

        boolean isAdmin = "ADMIN".equals(requesterRole);
        boolean isAuthor = review.getAuthorId().equals(requesterId);

        if (!isAdmin && !isAuthor) {
            throw new InsufficientPermissionsException("You can only delete your own reviews");
        }

        repository.deleteById(reviewId);

        log.info("Review {} deleted by userId={} role={}", reviewId, requesterId, requesterRole);
    }

    private Review findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
    }
}
