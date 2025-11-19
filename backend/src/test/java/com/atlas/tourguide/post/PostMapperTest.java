package com.atlas.tourguide.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.atlas.tourguide.post.dtos.CreatePostRequestDto;
import com.atlas.tourguide.post.dtos.PostDto;
import com.atlas.tourguide.post.dtos.UpdatePostRequestDto;
import com.atlas.tourguide.user.User;
import com.atlas.tourguide.category.Category;
import com.atlas.tourguide.tag.Tag;

public class PostMapperTest {

  private PostMapper postMapper;

  @BeforeEach
  public void setUp() {
    postMapper = Mappers.getMapper(PostMapper.class);
  }

  @Test
  public void testToDto_successfulMapping() {
    // Arrange
    UUID postId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    UUID tagId1 = UUID.randomUUID();
    UUID tagId2 = UUID.randomUUID();

    // Create a Post entity with all fields populated
    Post post = Post.builder().id(postId).title("Test Post Title").content("Test post content...")
        .status(PostStatus.PUBLISHED).author(User.builder().id(authorId).build())
        .category(Category.builder().id(categoryId).build())
        .tags(new HashSet<>(
            Set.of(Tag.builder().id(tagId1).build(), Tag.builder().id(tagId2).build())))
        .readingTime(10).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
        .latitude(40.7128).longitude(-74.0060).build();

    // Act
    PostDto postDto = postMapper.toDto(post);

    // Assert
    assertNotNull(postDto);
    assertEquals(post.getId(), postDto.getId());
    assertEquals(post.getTitle(), postDto.getTitle());
    assertEquals(post.getContent(), postDto.getContent());
    assertEquals(post.getStatus(), postDto.getStatus());
    assertEquals(post.getReadingTime(), postDto.getReadingTime());
    assertEquals(post.getCreatedAt(), postDto.getCreatedAt());
    assertEquals(post.getUpdatedAt(), postDto.getUpdatedAt());
    assertEquals(post.getLatitude(), postDto.getLatitude());
    assertEquals(post.getLongitude(), postDto.getLongitude());

    // Assert nested objects (author, category, tags)
    assertNotNull(postDto.getAuthor());
    assertEquals(post.getAuthor().getId(), postDto.getAuthor().getId());

    assertNotNull(postDto.getCategory());
    assertEquals(post.getCategory().getId(), postDto.getCategory().getId());

    assertNotNull(postDto.getTags());
    assertEquals(post.getTags().size(), postDto.getTags().size());
    assertTrue(postDto.getTags().stream().anyMatch(t -> t.getId().equals(tagId1)));
    assertTrue(postDto.getTags().stream().anyMatch(t -> t.getId().equals(tagId2)));
  }

  @Test
  public void testToCreatePostRequest_successfulMapping() {
    // Arrange
    UUID categoryId = UUID.randomUUID();
    Set<UUID> tagIds = new HashSet<>(Set.of(UUID.randomUUID(), UUID.randomUUID()));

    CreatePostRequestDto dto = CreatePostRequestDto.builder().title("New Post")
        .content("Content of the new post.").categoryId(categoryId).tagIds(tagIds)
        .status(PostStatus.DRAFT).latitude(34.0000).longitude(-6.0000).build();

    // Act
    CreatePostRequest request = postMapper.toCreatePostRequest(dto);

    // Assert
    assertNotNull(request);
    assertEquals(dto.getTitle(), request.getTitle());
    assertEquals(dto.getContent(), request.getContent());
    assertEquals(dto.getCategoryId(), request.getCategoryId());
    assertEquals(dto.getTagIds(), request.getTagIds());
    assertEquals(dto.getStatus(), request.getStatus());
    assertEquals(dto.getLatitude(), request.getLatitude());
    assertEquals(dto.getLongitude(), request.getLongitude());
  }

  @Test
  public void testToUpdatePostRequest_successfulMapping() {
    // Arrange
    UUID postId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    Set<UUID> tagIds = new HashSet<>(Set.of(UUID.randomUUID()));

    UpdatePostRequestDto dto = UpdatePostRequestDto.builder().id(postId).title("Updated Title")
        .content("Updated content.").categoryId(categoryId).tagIds(tagIds)
        .status(PostStatus.PUBLISHED).latitude(34.0538).longitude(-6.0799).build();

    // Act
    UpdatePostRequest request = postMapper.toUpdatePostRequest(dto);

    // Assert
    assertNotNull(request);
    assertEquals(dto.getId(), request.getId());
    assertEquals(dto.getTitle(), request.getTitle());
    assertEquals(dto.getContent(), request.getContent());
    assertEquals(dto.getCategoryId(), request.getCategoryId());
    assertEquals(dto.getTagIds(), request.getTagIds());
    assertEquals(dto.getStatus(), request.getStatus());
    assertEquals(dto.getLatitude(), request.getLatitude());
    assertEquals(dto.getLongitude(), request.getLongitude());
  }
}