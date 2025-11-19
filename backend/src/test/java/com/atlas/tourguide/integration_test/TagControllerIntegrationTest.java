package com.atlas.tourguide.integration_test;

import com.atlas.tourguide.integration_test.BaseIntegrationTest;
import com.atlas.tourguide.shared.exception.ApiErrorResponse;
import com.atlas.tourguide.tag.Tag;
import com.atlas.tourguide.tag.TagRepository;
import com.atlas.tourguide.tag.dtos.CreateTagsRequestDto;
import com.atlas.tourguide.tag.dtos.TagDto;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DisplayName("TagController Integration Tests")
public class TagControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private TagRepository tagRepository;
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
    tagRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("✅ [POST /tags] Should create new tags and return all requested tags")
  void createTags_withNewAndExistingNames_shouldSucceed() {
    // Arrange
    tagRepository.save(Tag.builder().name("Java").build()); // Pre-existing tag
    CreateTagsRequestDto requestDto = new CreateTagsRequestDto(
        Set.of("Java", "Spring Boot", "Testcontainers"));
    HttpEntity<CreateTagsRequestDto> requestEntity = new HttpEntity<>(requestDto, authHeaders);

    // Act
    ResponseEntity<List<TagDto>> response = restTemplate.exchange("/api/v1/tags", HttpMethod.POST,
        requestEntity, new ParameterizedTypeReference<>() {
        });

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull().hasSize(3);
    assertThat(tagRepository.count()).isEqualTo(3);
  }

  @Test
  @DisplayName("❌ [POST /tags] Should return 401 Unauthorized when not authenticated")
  void createTags_whenNotAuthenticated_shouldFail() {
    // Arrange
    CreateTagsRequestDto requestDto = new CreateTagsRequestDto(Set.of("Java"));
    HttpEntity<CreateTagsRequestDto> requestEntity = new HttpEntity<>(requestDto); // No auth
                                                                                   // headers

    // Act
    ResponseEntity<ApiErrorResponse> response = restTemplate.exchange("/api/v1/tags",
        HttpMethod.POST, requestEntity, ApiErrorResponse.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}