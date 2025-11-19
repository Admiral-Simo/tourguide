package com.atlas.tourguide.category;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Repository;

import com.atlas.tourguide.post.PostStatus;
import com.atlas.tourguide.category.dtos.CategoryDto;
import com.atlas.tourguide.category.dtos.CreateCategoryRequest;
import com.atlas.tourguide.post.Post;

@Repository
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
  @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
  CategoryDto toDto(Category category);

  Category toEntity(CreateCategoryRequest createCategoryRequest);

  @Named("calculatePostCount")
  default long calculatePostCount(List<Post> posts) {
    if (null == posts) {
      return 0;
    }
    return posts.stream().filter(post -> PostStatus.PUBLISHED.equals(post.getStatus())).count();
  }
}
