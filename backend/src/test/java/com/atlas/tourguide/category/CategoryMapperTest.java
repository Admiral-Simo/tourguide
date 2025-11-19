package com.atlas.tourguide.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import com.atlas.tourguide.post.PostStatus;
import com.atlas.tourguide.category.dtos.CategoryDto;
import com.atlas.tourguide.post.Post;

public class CategoryMapperTest {
  private CategoryMapper categoryMapper;

  @BeforeEach
  public void setUp() {
    categoryMapper = Mappers.getMapper(CategoryMapper.class);
  }

  @Test
  public void testToDto_withPublishedAndDraftPosts_shouldCalculateCorrectCount() {
    // Arrange
    UUID categoryId = UUID.randomUUID();
    String categoryName = "Nature";

    Category category = Category.builder().id(categoryId).name(categoryName)
        .posts(List.of(Post.builder().status(PostStatus.PUBLISHED).build(),
            Post.builder().status(PostStatus.PUBLISHED).build(),
            Post.builder().status(PostStatus.DRAFT).build()))
        .build();

    // Act
    CategoryDto dto = categoryMapper.toDto(category);

    // Assert
    assertNotNull(dto);
    assertEquals(categoryId, dto.getId());
    assertEquals(categoryName, dto.getName());
    assertEquals(2, dto.getPostCount()); // Expecting 2 published posts
  }

  @Test
  public void testToDto_withNoPublishedPosts_shouldReturnZero() {
    // Arrange
    UUID categoryId = UUID.randomUUID();
    String categoryName = "Drafts Only";

    Category category = Category.builder().id(categoryId).name(categoryName)
        .posts(List.of(Post.builder().status(PostStatus.DRAFT).build(),
            Post.builder().status(PostStatus.DRAFT).build()))
        .build();

    // Act
    CategoryDto dto = categoryMapper.toDto(category);

    // Assert
    assertNotNull(dto);
    assertEquals(0, dto.getPostCount());
  }

  @Test
  public void testToDto_withNullPostsList_shouldReturnZero() {
    // Arrange
    UUID categoryId = UUID.randomUUID();
    String categoryName = "Null Posts";

    Category category = Category.builder().id(categoryId).name(categoryName).posts(null).build();

    // Act
    CategoryDto dto = categoryMapper.toDto(category);

    // Assert
    assertNotNull(dto);
    assertEquals(0, dto.getPostCount());
  }
}