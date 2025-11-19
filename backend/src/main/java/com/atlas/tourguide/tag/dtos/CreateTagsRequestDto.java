package com.atlas.tourguide.tag.dtos;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTagsRequestDto {
  @NotEmpty(message = "At least one is required")
  @Size(max = 10, message = "Maximum {max} tags allowed")
  private Set<@Size(min = 2, max = 50, message = "Tag name must be between {min} and {max} characters") @Pattern(regexp = "^[\\w\\s-]+$", message = "Tag name can only contain letters, numbers, spaces, and hyphens") String> names;
}
