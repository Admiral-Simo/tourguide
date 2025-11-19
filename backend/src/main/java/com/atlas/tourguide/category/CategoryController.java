package com.atlas.tourguide.category;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atlas.tourguide.category.dtos.CategoryDto;
import com.atlas.tourguide.category.dtos.CreateCategoryRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(path = "/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService categoryService;
  private final CategoryMapper categoryMapper;

  @GetMapping
  public ResponseEntity<List<CategoryDto>> listCategories() {
    List<CategoryDto> categories = categoryService.listCategories().stream()
        .map(categoryMapper::toDto).toList();
    return ResponseEntity.ok(categories);
  }

  @PostMapping
  public ResponseEntity<CategoryDto> createCategory(
      @Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
    Category category = categoryMapper.toEntity(createCategoryRequest);
    Category savedCategory = categoryService.createCategory(category);
    return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toDto(savedCategory)

    );
  }

  @DeleteMapping(path = "/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
    categoryService.deleteCategory(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
