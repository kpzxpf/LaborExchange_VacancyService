package com.vlz.ladorexchange_vacancyservice.mapper;

import com.vlz.ladorexchange_vacancyservice.dto.ReviewDto;
import com.vlz.ladorexchange_vacancyservice.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    ReviewDto toDto(Review review);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toEntity(ReviewDto dto);

    List<ReviewDto> toDtoList(List<Review> reviews);
}
