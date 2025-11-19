package com.atlas.tourguide.post;

import com.atlas.tourguide.category.Category;
import com.atlas.tourguide.category.CategoryService;
import com.atlas.tourguide.tag.Tag;
import com.atlas.tourguide.tag.TagService;
import com.atlas.tourguide.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Unit Tests")
class PostServiceTest {

  @Mock
  private PostRepository postRepository;
  @Mock
  private CategoryService categoryService;
  @Mock
  private TagService tagService;

  @InjectMocks
  private PostServiceImpl postService;

  private User user;
  private Category category;
  private Tag tag;

  @BeforeEach
  void setUp() {
    user = User.builder().id(UUID.randomUUID()).name("Test User").build();
    category = Category.builder().id(UUID.randomUUID()).name("Technology").build();
    tag = Tag.builder().id(UUID.randomUUID()).name("Java").build();
  }

  @Nested
  @DisplayName("getAllPosts Method")
  class GetAllPosts {
    @Test
        @DisplayName("should return all published posts when no filters are provided")
        void whenNoFilters_shouldReturnAllPublished() {
            // Arrange
            when(postRepository.findAllByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(new Post()));

            // Act
            List<Post> result = postService.getAllPosts(null, null);

            // Assert
            assertThat(result).hasSize(1);
            verify(postRepository).findAllByStatus(PostStatus.PUBLISHED);
            verifyNoInteractions(categoryService, tagService);
        }

    @Test
        @DisplayName("should filter by category when only categoryId is provided")
        void whenCategoryFilter_shouldFilterByCategory() {
            // Arrange
            when(categoryService.getCategoryById(category.getId())).thenReturn(category);
            when(postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, category)).thenReturn(List.of(new Post()));

            // Act
            List<Post> result = postService.getAllPosts(category.getId(), null);

            // Assert
            assertThat(result).hasSize(1);
            verify(postRepository).findAllByStatusAndCategory(PostStatus.PUBLISHED, category);
            verify(categoryService).getCategoryById(category.getId());
            verifyNoInteractions(tagService);
        }

    @Test
        @DisplayName("should filter by tag when only tagId is provided")
        void whenTagFilter_shouldFilterByTag() {
            // Arrange
            when(tagService.getTagById(tag.getId())).thenReturn(tag);
            when(postRepository.findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag)).thenReturn(List.of(new Post()));

            // Act
            List<Post> result = postService.getAllPosts(null, tag.getId());

            // Assert
            assertThat(result).hasSize(1);
            verify(postRepository).findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag);
            verify(tagService).getTagById(tag.getId());
            verifyNoInteractions(categoryService);
        }

    @Test
        @DisplayName("should filter by category and tag when both IDs are provided")
        void whenBothFilters_shouldFilterByCategoryAndTag() {
            // Arrange
            when(categoryService.getCategoryById(category.getId())).thenReturn(category);
            when(tagService.getTagById(tag.getId())).thenReturn(tag);
            when(postRepository.findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, category, tag)).thenReturn(List.of(new Post()));

            // Act
            List<Post> result = postService.getAllPosts(category.getId(), tag.getId());

            // Assert
            assertThat(result).hasSize(1);
            verify(postRepository).findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, category, tag);
        }
  }

  @Nested
  @DisplayName("createPost Method")
  class CreatePost {
    @Test
    @DisplayName("should create and save a new post with correct details")
    void shouldCreateAndSavePost() {
      // Arrange
      String content = "This is some content with exactly eight words.";
      CreatePostRequest request = CreatePostRequest.builder().title("New Post").content(content)
          .status(PostStatus.DRAFT).categoryId(category.getId()).tagIds(Set.of(tag.getId()))
          .latitude(40.7128).longitude(-74.0060).build();

      when(categoryService.getCategoryById(category.getId())).thenReturn(category);
      when(tagService.getTagByIds(Set.of(tag.getId()))).thenReturn(List.of(tag));
      when(postRepository.save(any(Post.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      Post createdPost = postService.createPost(user, request);

      // Assert
      ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
      verify(postRepository).save(postCaptor.capture());
      Post capturedPost = postCaptor.getValue();

      assertThat(createdPost).isNotNull();
      assertThat(capturedPost.getTitle()).isEqualTo("New Post");
      assertThat(capturedPost.getAuthor()).isEqualTo(user);
      assertThat(capturedPost.getCategory()).isEqualTo(category);
      assertThat(capturedPost.getTags()).contains(tag);
      assertThat(capturedPost.getStatus()).isEqualTo(PostStatus.DRAFT);
      assertThat(capturedPost.getReadingTime()).isEqualTo(1); // 8 words / 200 wpm, ceiling is 1
    }
  }

  @Nested
  @DisplayName("updatePost Method")
  class UpdatePost {
    @Test
    @DisplayName("should update an existing post")
    void shouldUpdateExistingPost() {
      // Arrange
      UUID postId = UUID.randomUUID();
      Post existingPost = Post.builder().id(postId).title("Old Title").content("Old Content")
          .category(category).tags(Collections.emptySet()).build();
      UpdatePostRequest request = UpdatePostRequest.builder().title("New Title")
          .content("New Content").categoryId(category.getId()).tagIds(Collections.emptySet())
          .build();

      when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
      when(postRepository.save(any(Post.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      Post updatedPost = postService.updatePost(postId, request);

      // Assert
      assertThat(updatedPost.getTitle()).isEqualTo("New Title");
      assertThat(updatedPost.getContent()).isEqualTo("New Content");
      verify(postRepository).save(existingPost);
    }

    @Test
    @DisplayName("should throw EntityNotFoundException if post to update does not exist")
    void shouldThrowExceptionWhenPostNotFound() {
      // Arrange
      UUID postId = UUID.randomUUID();
      when(postRepository.findById(postId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> postService.updatePost(postId, new UpdatePostRequest()))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessage("Post does not exist with id " + postId);
    }
  }

  @Nested
  @DisplayName("deletePost Method")
  class DeletePost {
    @Test
    @DisplayName("should delete the post when it exists")
    void shouldDeleteExistingPost() {
      // Arrange
      UUID postId = UUID.randomUUID();
      Post post = Post.builder().id(postId).build();
      when(postRepository.findById(postId)).thenReturn(Optional.of(post));

      // Act
      postService.deletePost(postId);

      // Assert
      verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("should throw EntityNotFoundException if post to delete does not exist")
    void shouldThrowExceptionWhenDeletingNonExistentPost() {
      // Arrange
      UUID postId = UUID.randomUUID();
      when(postRepository.findById(postId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> postService.deletePost(postId))
          .isInstanceOf(EntityNotFoundException.class);

      verify(postRepository, never()).delete(any());
    }
  }
}