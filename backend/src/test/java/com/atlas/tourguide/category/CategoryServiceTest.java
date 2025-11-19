package com.atlas.tourguide.category;

import com.atlas.tourguide.post.Post;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryServiceImpl categoryService;

  @Test
  @DisplayName("listCategories should call repository and return all categories")
  void listCategories_ShouldReturnAllCategories() {
    // Arrange
    List<Category> expectedCategories = List.of(new Category(), new Category());
    when(categoryRepository.findAllWithPostCount()).thenReturn(expectedCategories);

    // Act
    List<Category> actualCategories = categoryService.listCategories();

    // Assert
    assertThat(actualCategories).isEqualTo(expectedCategories);
    verify(categoryRepository, times(1)).findAllWithPostCount();
  }

  @Test
  @DisplayName("createCategory should save a new category if the name does not exist")
  void createCategory_WhenNameIsUnique_ShouldSaveCategory() {
    // Arrange
    Category newCategory = Category.builder().name("New Category").build();
    when(categoryRepository.existsByNameIgnoreCase("New Category")).thenReturn(false);
    when(categoryRepository.save(newCategory)).thenReturn(newCategory);

    // Act
    Category savedCategory = categoryService.createCategory(newCategory);

    // Assert
    assertThat(savedCategory).isNotNull();
    assertThat(savedCategory.getName()).isEqualTo("New Category");
    verify(categoryRepository, times(1)).existsByNameIgnoreCase("New Category");
    verify(categoryRepository, times(1)).save(newCategory);
  }

  @Test
  @DisplayName("createCategory should throw IllegalArgumentException if the name already exists")
  void createCategory_WhenNameExists_ShouldThrowException() {
    // Arrange
    Category duplicateCategory = Category.builder().name("Existing Category").build();
    when(categoryRepository.existsByNameIgnoreCase("Existing Category")).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> categoryService.createCategory(duplicateCategory))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Category already exists with name: Existing Category");

    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  @DisplayName("deleteCategory should delete the category when it has no posts")
  void deleteCategory_WhenCategoryIsEmpty_ShouldDelete() {
    // Arrange
    UUID categoryId = UUID.randomUUID();
    Category category = Category.builder().id(categoryId).posts(Collections.emptyList()).build();
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

    // Act
    categoryService.deleteCategory(categoryId);

    // Assert
    verify(categoryRepository, times(1)).findById(categoryId);
    verify(categoryRepository, times(1)).deleteById(categoryId);
  }

  @Test
  @DisplayName("deleteCategory should throw IllegalStateException when the category has posts")
  void deleteCategory_WhenCategoryHasPosts_ShouldThrowException() {
    // Arrange
    UUID categoryId = UUID.randomUUID();
    Category categoryWithPosts = Category.builder().id(categoryId).posts(List.of(new Post()))
        .build();
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryWithPosts));

    // Act & Assert
    assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Category has posts associated with it");

    verify(categoryRepository, never()).deleteById(any(UUID.class));
  }

  @Test
  @DisplayName("deleteCategory should do nothing if the category does not exist")
  void deleteCategory_WhenCategoryNotFound_ShouldDoNothing() {
    // Arrange
    UUID categoryId = UUID.randomUUID();
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    // Act
    categoryService.deleteCategory(categoryId);

    // Assert
    verify(categoryRepository, times(1)).findById(categoryId);
    verify(categoryRepository, never()).deleteById(any(UUID.class));
  }

  @Test
  @DisplayName("getCategoryById should return the category when found")
  void getCategoryById_WhenFound_ShouldReturnCategory() {
    // Arrange
    UUID categoryId = UUID.randomUUID();
    Category expectedCategory = Category.builder().id(categoryId).name("Found Category").build();
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(expectedCategory));

    // Act
    Category actualCategory = categoryService.getCategoryById(categoryId);

    // Assert
    assertThat(actualCategory).isEqualTo(expectedCategory);
  }

  @Test
  @DisplayName("getCategoryById should throw EntityNotFoundException when not found")
  void getCategoryById_WhenNotFound_ShouldThrowException() {
    // Arrange
    UUID categoryId = UUID.randomUUID();
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Category not found with id: " + categoryId);
  }
}