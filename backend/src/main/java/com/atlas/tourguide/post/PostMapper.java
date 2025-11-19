package com.atlas.tourguide.post;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.atlas.tourguide.post.dtos.CreatePostRequestDto;
import com.atlas.tourguide.post.dtos.PostDto;
import com.atlas.tourguide.post.dtos.UpdatePostRequestDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {
  @Mapping(target = "author", source = "author")
  @Mapping(target = "category", source = "category")
  @Mapping(target = "tags", source = "tags")
  PostDto toDto(Post post);

  CreatePostRequest toCreatePostRequest(CreatePostRequestDto dto);
  UpdatePostRequest toUpdatePostRequest(UpdatePostRequestDto dto);
}
