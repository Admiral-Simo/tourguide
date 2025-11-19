package com.atlas.tourguide.category;

import com.atlas.tourguide.category.dtos.CategoryDto;
import com.atlas.tourguide.category.dtos.CreateCategoryRequest;
import com.atlas.tourguide.shared.exception.ErrorController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController Unit Tests")
class CategoryControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CategoryService categoryService;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        // Manually build MockMvc to test the controller in isolation
        // and register our custom exception handler.
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new ErrorController())
                .build();
    }

    @Test
    @DisplayName("✅ [GET /categories] should return a list of category DTOs and 200 OK")
    void listCategories_ShouldReturnListOfCategories() throws Exception {
        // Arrange
        Category category1 = Category.builder().id(UUID.randomUUID()).name("Technology").build();
        CategoryDto categoryDto1 = new CategoryDto(category1.getId(), category1.getName(), 5);

        when(categoryService.listCategories()).thenReturn(List.of(category1));
        when(categoryMapper.toDto(category1)).thenReturn(categoryDto1);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Technology"))
                .andExpect(jsonPath("$[0].postCount").value(5));

        verify(categoryService, times(1)).listCategories();
    }

    @Test
    @DisplayName("✅ [POST /categories] should create a new category and return 201 Created")
    void createCategory_WithValidRequest_ShouldReturnCreated() throws Exception {
        // Arrange
        CreateCategoryRequest requestDto = new CreateCategoryRequest("New Category");
        Category categoryEntity = Category.builder().name("New Category").build();
        Category savedCategory = Category.builder().id(UUID.randomUUID()).name("New Category").build();
        CategoryDto responseDto = new CategoryDto(savedCategory.getId(), savedCategory.getName(), 0);

        when(categoryMapper.toEntity(any(CreateCategoryRequest.class))).thenReturn(categoryEntity);
        when(categoryService.createCategory(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toDto(any(Category.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedCategory.getId().toString()))
                .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    @DisplayName("❌ [POST /categories] should return 400 Bad Request when name is blank")
    void createCategory_WithBlankName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCategoryRequest requestDto = new CreateCategoryRequest(""); // Invalid name

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest()); // Bean validation is handled automatically
    }

    @Test
    @DisplayName("❌ [POST /categories] should return 400 Bad Request when category name already exists")
    void createCategory_WhenNameExists_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCategoryRequest requestDto = new CreateCategoryRequest("Duplicate");

        when(categoryMapper.toEntity(any(CreateCategoryRequest.class))).thenReturn(new Category());
        when(categoryService.createCategory(any(Category.class)))
                .thenThrow(new IllegalArgumentException("Category already exists with name: Duplicate"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category already exists with name: Duplicate"));
    }

    @Test
    @DisplayName("✅ [DELETE /categories/{id}] should delete category and return 204 No Content")
    void deleteCategory_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        doNothing().when(categoryService).deleteCategory(categoryId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(categoryId);
    }

    @Test
    @DisplayName("❌ [DELETE /categories/{id}] should return 409 Conflict when category has posts")
    void deleteCategory_WhenHasPosts_ShouldReturnConflict() throws Exception {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        doThrow(new IllegalStateException("Category has posts associated with it"))
                .when(categoryService).deleteCategory(categoryId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category has posts associated with it"));
    }
}