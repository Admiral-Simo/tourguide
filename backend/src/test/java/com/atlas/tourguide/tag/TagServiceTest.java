package com.atlas.tourguide.tag;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.atlas.tourguide.post.Post;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService Unit Tests")
class TagServiceTest {
  @Mock
  private TagRepository tagRepository;

  @InjectMocks
  private TagServiceImpl tagService;

  @Test
  @DisplayName("getTags should call repository and return all tags")
  void getTags_ShouldReturnAllTags() {
    // Arrange
    List<Tag> expectedTags = List.of(new Tag(), new Tag());
    when(tagRepository.findAllWithPostCount()).thenReturn(expectedTags);

    // Act
    List<Tag> actualTags = tagService.getTags();

    // Assert
    assertThat(actualTags).isEqualTo(expectedTags);
    verify(tagRepository, times(1)).findAllWithPostCount();
  }

  @Test
  @DisplayName("createTags should create only new tags when a mix of new and existing are provided")
  void createTags_WhenMixOfNewAndExisting_ShouldCreateOnlyNewTags() {
    // Arrange
    Set<String> tagNames = Set.of("Java", "Spring", "Docker");
    Tag existingTag = Tag.builder().id(UUID.randomUUID()).name("Java").build();

    when(tagRepository.findByNameIn(tagNames)).thenReturn(List.of(existingTag));
    // Capture what is passed to saveAll
    ArgumentCaptor<List<Tag>> captor = ArgumentCaptor.forClass(List.class);
    when(tagRepository.saveAll(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    List<Tag> result = tagService.createTags(tagNames);

    // Assert
    // Verify saveAll was called once
    verify(tagRepository, times(1)).saveAll(any());
    // Verify that the list passed to saveAll contains only the new tags
    List<Tag> savedTags = captor.getValue();
    assertThat(savedTags).hasSize(2);
    Set<String> savedTagNames = savedTags.stream().map(Tag::getName).collect(Collectors.toSet());
    assertThat(savedTagNames).containsExactlyInAnyOrder("Spring", "Docker");

    // Verify the final returned list has all 3 tags
    assertThat(result).hasSize(3);
  }

  @Test
  @DisplayName("createTags should not call saveAll when all tags already exist")
  void createTags_WhenAllExist_ShouldNotCallSave() {
    // Arrange
    Set<String> tagNames = Set.of("Java", "Spring");
    List<Tag> existingTags = List.of(Tag.builder().id(UUID.randomUUID()).name("Java").build(),
        Tag.builder().id(UUID.randomUUID()).name("Spring").build());
    when(tagRepository.findByNameIn(tagNames)).thenReturn(existingTags);

    // Act
    List<Tag> result = tagService.createTags(tagNames);

    // Assert
    verify(tagRepository, never()).saveAll(any());
    assertThat(result).hasSize(2);
    assertThat(result).containsAll(existingTags);
  }

  @Test
  @DisplayName("deleteTag should delete tag when it has no associated posts")
  void deleteTag_WhenTagIsEmpty_ShouldDelete() {
    // Arrange
    UUID tagId = UUID.randomUUID();
    Tag tagToDelete = Tag.builder().id(tagId).name("Old Tag").posts(Collections.emptySet()).build();
    when(tagRepository.findById(tagId)).thenReturn(Optional.of(tagToDelete));

    // Act
    tagService.deleteTag(tagId);

    // Assert
    verify(tagRepository, times(1)).findById(tagId);
    verify(tagRepository, times(1)).deleteById(tagId);
  }

  @Test
  @DisplayName("deleteTag should throw IllegalStateException when tag has posts")
  void deleteTag_WhenTagHasPosts_ShouldThrowException() {
    // Arrange
    UUID tagId = UUID.randomUUID();
    Tag tagWithPosts = Tag.builder().id(tagId).name("Popular Tag").posts(Set.of(new Post()))
        .build();
    when(tagRepository.findById(tagId)).thenReturn(Optional.of(tagWithPosts));

    // Act & Assert
    assertThatThrownBy(() -> tagService.deleteTag(tagId)).isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot delete tag with posts");

    verify(tagRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("deleteTag should do nothing if tag does not exist")
  void deleteTag_WhenTagNotFound_ShouldDoNothing() {
    // Arrange
    UUID tagId = UUID.randomUUID();
    when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

    // Act
    tagService.deleteTag(tagId);

    // Assert
    verify(tagRepository, times(1)).findById(tagId);
    verify(tagRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("getTagById should return tag when found")
  void getTagById_WhenFound_ShouldReturnTag() {
    // Arrange
    UUID tagId = UUID.randomUUID();
    Tag expectedTag = Tag.builder().id(tagId).name("My Tag").build();
    when(tagRepository.findById(tagId)).thenReturn(Optional.of(expectedTag));

    // Act
    Tag actualTag = tagService.getTagById(tagId);

    // Assert
    assertThat(actualTag).isEqualTo(expectedTag);
  }

  @Test
  @DisplayName("getTagById should throw EntityNotFoundException when not found")
  void getTagById_WhenNotFound_ShouldThrowException() {
    // Arrange
    UUID tagId = UUID.randomUUID();
    when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> tagService.getTagById(tagId))
        .isInstanceOf(EntityNotFoundException.class).hasMessage("Tag not found with id: " + tagId);
  }

  @Test
  @DisplayName("getTagByIds should return all tags when all IDs are found")
  void getTagByIds_WhenAllFound_ShouldReturnTags() {
    // Arrange
    Set<UUID> tagIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
    List<Tag> expectedTags = List.of(new Tag(), new Tag());
    when(tagRepository.findAllById(tagIds)).thenReturn(expectedTags);

    // Act
    List<Tag> actualTags = tagService.getTagByIds(tagIds);

    // Assert
    assertThat(actualTags).hasSize(2).isEqualTo(expectedTags);
  }

  @Test
  @DisplayName("getTagByIds should throw EntityNotFoundException if not all IDs are found")
  void getTagByIds_WhenSomeNotFound_ShouldThrowException() {
    // Arrange
    Set<UUID> tagIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
    List<Tag> foundTags = List.of(new Tag()); // Only one tag found
    when(tagRepository.findAllById(tagIds)).thenReturn(foundTags);

    // Act & Assert
    assertThatThrownBy(() -> tagService.getTagByIds(tagIds))
        .isInstanceOf(EntityNotFoundException.class).hasMessage("Not all specified tag IDs exist");
  }
}