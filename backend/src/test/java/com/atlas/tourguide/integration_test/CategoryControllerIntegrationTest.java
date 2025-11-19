package com.atlas.tourguide.integration_test;

import com.atlas.tourguide.category.Category;
import com.atlas.tourguide.category.CategoryRepository;
import com.atlas.tourguide.category.dtos.CategoryDto;
import com.atlas.tourguide.post.Post;
import com.atlas.tourguide.post.PostRepository;
import com.atlas.tourguide.post.PostStatus;
import com.atlas.tourguide.shared.exception.ApiErrorResponse;
import com.atlas.tourguide.user.User;
import com.atlas.tourguide.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DisplayName("CategoryController Integration Tests")
public class CategoryControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private HttpHeaders authHeaders;

  @BeforeEach
  void setUp() {
    User user = User.builder().name("Test User").email("user@example.com")
        .password(passwordEncoder.encode("pass")).build();
    userRepository.save(user);
    authHeaders = getAuthHeaders("user@example.com", "pass");
  }

  @AfterEach
  void tearDown() {
    postRepository.deleteAll();
    userRepository.deleteAll();
    categoryRepository.deleteAll();
  }

  @Test
  @DisplayName("✅ [GET /categories] Should return list of categories with correct published post counts")
  void listCategories_shouldReturnCategoriesWithPostCounts() {
    // Arrange
    Category tech = categoryRepository.save(Category.builder().name("Technology").build());
    Category travel = categoryRepository.save(Category.builder().name("Travel").build());
    User author = userRepository.findByEmail("user@example.com").get();

    postRepository.save(Post.builder().title("...").content("...").author(author).category(tech)
        .status(PostStatus.PUBLISHED).readingTime(1).latitude(0.0).longitude(0.0).build());
    postRepository.save(Post.builder().title("...").content("...").author(author).category(tech)
        .status(PostStatus.PUBLISHED).readingTime(1).latitude(0.0).longitude(0.0).build());
    postRepository.save(Post.builder().title("...").content("...").author(author).category(tech)
        .status(PostStatus.DRAFT).readingTime(1).latitude(0.0).longitude(0.0).build());

    // Act
    ResponseEntity<List<CategoryDto>> response = restTemplate.exchange("/api/v1/categories",
        HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull().hasSize(2);

    CategoryDto techDto = response.getBody().stream().filter(c -> c.getName().equals("Technology"))
        .findFirst().get();
    CategoryDto travelDto = response.getBody().stream().filter(c -> c.getName().equals("Travel"))
        .findFirst().get();

    assertThat(techDto.getPostCount()).as("Post count should only include PUBLISHED posts")
        .isEqualTo(2);
    assertThat(travelDto.getPostCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("❌ [DELETE /categories/{id}] Should return 409 Conflict when deleting a category with posts")
  void deleteCategory_whenHasPosts_shouldReturnConflict() {
    // Arrange
    Category category = categoryRepository.save(Category.builder().name("To Be Deleted").build());
    User author = userRepository.findByEmail("user@example.com").get();
    postRepository.save(Post.builder().title("...").content("...").author(author).category(category)
        .status(PostStatus.PUBLISHED).readingTime(1).latitude(0.0).longitude(0.0).build());

    HttpEntity<Void> requestEntity = new HttpEntity<>(authHeaders);

    // Act
    ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
        "/api/v1/categories/" + category.getId(), HttpMethod.DELETE, requestEntity,
        ApiErrorResponse.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Category has posts associated with it");
  }
}