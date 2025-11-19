package com.atlas.tourguide.tag;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.atlas.tourguide.post.PostStatus;
import com.atlas.tourguide.tag.dtos.TagDto;
import com.atlas.tourguide.post.Post;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
  @Mapping(target = "postCount", source = "posts", qualifiedByName = "calculatePostCount")
  TagDto toTagResponse(Tag tag);

  @Named("calculatePostCount")
  default long calculatePostCount(Set<Post> posts) {
    if (posts == null) {
      return 0;
    }

    return posts.stream().filter(post -> post.getStatus().equals(PostStatus.PUBLISHED)).count();
  }
}
