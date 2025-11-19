package com.atlas.tourguide.post;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {
  private String title;

  private String content;

  private UUID categoryId;

  @Builder.Default
  private Set<UUID> tagIds = new HashSet<>();

  private PostStatus status;

  private Double latitude; // Latitude of the post location

  private Double longitude; // Longitude of the post location
}
