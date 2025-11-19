package com.atlas.tourguide.integration_test;

import com.atlas.tourguide.auth.dtos.AuthResponseDto;
import com.atlas.tourguide.auth.dtos.LoginRequestDto;
import com.atlas.tourguide.auth.dtos.SignupRequestDto;
import com.atlas.tourguide.shared.exception.ApiErrorResponse;
import com.atlas.tourguide.user.User;
import com.atlas.tourguide.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("AuthController Integration Tests")
public class AuthControllerIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    registry.add("jwt.secret", () -> "a-very-long-and-secure-secret-key-for-testing-purposes");
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  @Nested
  @DisplayName("User Signup Scenarios")
  class SignupScenarios {

    @Test
    @DisplayName("✅ Should create user and return token on valid signup")
    void signup_WithValidData_ShouldCreateUserAndReturnToken() {
      // Arrange
      SignupRequestDto signupRequest = new SignupRequestDto("test@example.com", "password123",
          "Test User");

      // Act
      ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity("/api/v1/auth/signup",
          signupRequest, AuthResponseDto.class);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getToken()).isNotBlank();

      // Verify database state
      Optional<User> createdUserOpt = userRepository.findByEmail("test@example.com");
      assertThat(createdUserOpt).isPresent();
      User createdUser = createdUserOpt.get();
      assertThat(createdUser.getName()).isEqualTo("Test User");
      assertThat(createdUser.getPassword()).startsWith("{bcrypt}"); // Verify delegating encoder is
                                                                    // used
    }

    @Test
    @DisplayName("❌ Should return 400 Bad Request if email already exists")
    void signup_WhenEmailAlreadyExists_ShouldReturn400BadRequest() {
      // Arrange: Pre-populate the database with a user
      User existingUser = User.builder().name("Existing User").email("existing@example.com")
          .password("some-password").build();
      userRepository.save(existingUser);

      SignupRequestDto signupRequest = new SignupRequestDto("existing@example.com", "password123",
          "New User");

      // Act
      ResponseEntity<ApiErrorResponse> response = restTemplate.postForEntity("/api/v1/auth/signup",
          signupRequest, ApiErrorResponse.class);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).isEqualTo("Email is already in use.");
      assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
  }

  @Nested
  @DisplayName("User Login Scenarios")
  class LoginScenarios {

    @Test
    @DisplayName("✅ Should return token on valid credentials")
    void login_WithValidCredentials_ShouldReturnToken() {
      // Arrange: Pre-populate the database with a user
      User existingUser = User.builder().name("Test User").email("test@example.com")
          .password(passwordEncoder.encode("password123")) // Use the app's encoder
          .build();
      userRepository.save(existingUser);

      LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "password123");

      // Act
      ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity("/api/v1/auth/login",
          loginRequest, AuthResponseDto.class);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getToken()).isNotBlank();
    }

    @Test
    @DisplayName("❌ Should return 401 Unauthorized on incorrect password")
    void login_WithIncorrectPassword_ShouldReturn401Unauthorized() {
      // Arrange: Pre-populate the database with a user
      User existingUser = User.builder().name("Test User").email("test@example.com")
          .password(passwordEncoder.encode("password123")).build();
      userRepository.save(existingUser);

      LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "wrong-password");

      // Act
      ResponseEntity<ApiErrorResponse> response = restTemplate.postForEntity("/api/v1/auth/login",
          loginRequest, ApiErrorResponse.class);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).isEqualTo("Incorrect username or password.");
      assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("❌ Should return 401 Unauthorized for a non-existent user")
    void login_WithNonExistentUser_ShouldReturn401Unauthorized() {
      // Arrange
      LoginRequestDto loginRequest = new LoginRequestDto("nobody@example.com", "any-password");

      // Act
      ResponseEntity<ApiErrorResponse> response = restTemplate.postForEntity("/api/v1/auth/login",
          loginRequest, ApiErrorResponse.class);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getMessage()).isEqualTo("Incorrect username or password.");
    }
  }
}