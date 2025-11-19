package com.atlas.tourguide.post.dtos;

import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

import com.atlas.tourguide.post.PostStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequestDto {
  @NotBlank(message = "Title is required")
  @Size(min = 3, max = 200, message = "Title must be between {min} and {max} characters")
  private String title;

  @NotBlank(message = "Content is required")
  @Size(min = 10, max = 50000, message = "Content must be between {min} and {max} characters")
  private String content;

  @NotNull(message = "Category id is required")
  private UUID categoryId;

  @Builder.Default
  @Size(max = 10, message = "Maximum {max} tags allowed")
  private Set<UUID> tagIds = new HashSet<>();

  @NotNull(message = "Status is required")
  private PostStatus status;

  @NotNull(message = "Latitude is required")
  private Double latitude; // Latitude of the post location

  @NotNull(message = "Longitude is required")
  private Double longitude; // Longitude of the post location
}
