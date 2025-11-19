package com.atlas.tourguide.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.atlas.tourguide.post.PostStatus;
import com.atlas.tourguide.tag.dtos.TagDto;
import com.atlas.tourguide.post.Post;

public class TagMapperTest {

  private TagMapper tagMapper;

  @BeforeEach
  public void setUp() {
    // Get the generated mapper implementation
    tagMapper = Mappers.getMapper(TagMapper.class);
  }

  @Test
  public void givenTagWithPublishedPosts_whenMapped_thenPostCountIsCorrect() {
    // Given
    UUID tagId = UUID.randomUUID();
    String tagName = "TestTag";

    // Create a Tag entity
    Tag tag = Tag.builder().id(tagId).name(tagName).posts(new HashSet<>()).build();

    // Create a set of posts, some published and some not
    Set<Post> posts = new HashSet<>();
    posts.add(Post.builder().id(UUID.randomUUID()).status(PostStatus.PUBLISHED).build());
    posts.add(Post.builder().id(UUID.randomUUID()).status(PostStatus.PUBLISHED).build());
    posts.add(Post.builder().id(UUID.randomUUID()).status(PostStatus.DRAFT).build());
    tag.setPosts(posts);

    // When
    TagDto tagDto = tagMapper.toTagResponse(tag);

    // Then
    assertNotNull(tagDto);
    assertEquals(tagId, tagDto.getId());
    assertEquals(tagName, tagDto.getName());
    assertEquals(2, tagDto.getPostCount()); // Expecting 2 published posts
  }

  @Test
  public void givenTagWithNoPublishedPosts_whenMapped_thenPostCountIsZero() {
    // Given
    UUID tagId = UUID.randomUUID();
    String tagName = "AnotherTag";

    Tag tag = Tag.builder().id(tagId).name(tagName).posts(new HashSet<>()).build();

    Set<Post> posts = new HashSet<>();
    posts.add(Post.builder().id(UUID.randomUUID()).status(PostStatus.DRAFT).build());
    posts.add(Post.builder().id(UUID.randomUUID()).status(PostStatus.DRAFT).build());
    tag.setPosts(posts);

    // When
    TagDto tagDto = tagMapper.toTagResponse(tag);

    // Then
    assertNotNull(tagDto);
    assertEquals(0, tagDto.getPostCount()); // No published posts
  }

  @Test
  public void givenTagWithNullPosts_whenMapped_thenPostCountIsZero() {
    // Given
    UUID tagId = UUID.randomUUID();
    String tagName = "NullPostsTag";

    Tag tag = Tag.builder().id(tagId).name(tagName).posts(null) // Posts are null
        .build();

    // When
    TagDto tagDto = tagMapper.toTagResponse(tag);

    // Then
    assertNotNull(tagDto);
    assertEquals(0, tagDto.getPostCount());
  }
}