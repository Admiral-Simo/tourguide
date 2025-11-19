package com.atlas.tourguide.post;

import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {
  private UUID id;

  private String title;

  private String content;

  private UUID categoryId;

  @Builder.Default
  private Set<UUID> tagIds = new HashSet<>();

  private Double latitude; // Latitude of the post location

  private Double longitude; // Longitude of the post location

  private PostStatus status;
}
