package com.vlz.ladorexchange_vacancyservice.repository;

import com.vlz.ladorexchange_vacancyservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByCompanyId(Long companyId, Pageable pageable);
    Optional<Review> findByCompanyIdAndAuthorId(Long companyId, Long authorId);
    boolean existsByCompanyIdAndAuthorId(Long companyId, Long authorId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.companyId = :companyId")
    Optional<Double> findAverageRatingByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.companyId = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.companyId = :companyId GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> findRatingDistributionByCompanyId(@Param("companyId") Long companyId);
}
