package com.atlas.tourguide.integration_test;

import com.atlas.tourguide.user.User;
import com.atlas.tourguide.user.UserRepository;
import com.atlas.tourguide.user.dtos.UserProfileDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DisplayName("UserController Integration Tests")
public class UserControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private User testUser;
  private HttpHeaders authHeaders;

  @BeforeEach
  void setUp() {
    // Create a user to be authenticated for the tests
    testUser = User.builder().name("Test User").email("test@example.com")
        .password(passwordEncoder.encode("password123")).build();
    userRepository.save(testUser);

    // Get the authentication headers for this user
    authHeaders = getAuthHeaders("test@example.com", "password123");
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("✅ [GET /me] Should return current user's profile when authenticated")
  void getCurrentUser_whenAuthenticated_shouldReturnCorrectUserProfile() {
    // Arrange
    HttpEntity<Void> requestEntity = new HttpEntity<>(authHeaders);

    // Act
    ResponseEntity<UserProfileDto> response = restTemplate.exchange("/api/v1/user/me",
        HttpMethod.GET, requestEntity, UserProfileDto.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(testUser.getId());
    assertThat(response.getBody().email()).isEqualTo("test@example.com");
    assertThat(response.getBody().name()).isEqualTo("Test User");
  }

  @Test
  @DisplayName("❌ [GET /me] Should return 403 Forbidden when not authenticated")
  void getCurrentUser_whenNotAuthenticated_shouldReturn403Forbidden() {
    // Arrange
    HttpEntity<Void> requestEntity = new HttpEntity<>(new HttpHeaders()); // No auth token

    // Act
    ResponseEntity<Void> response = restTemplate.exchange("/api/v1/user/me", HttpMethod.GET,
        requestEntity, Void.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}