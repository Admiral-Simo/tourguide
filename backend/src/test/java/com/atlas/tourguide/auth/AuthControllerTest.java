package com.atlas.tourguide.auth;

import com.atlas.tourguide.auth.dtos.LoginRequestDto;
import com.atlas.tourguide.auth.dtos.SignupRequestDto;
import com.atlas.tourguide.shared.exception.ErrorController;
import com.atlas.tourguide.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests (Modern)")
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new ErrorController()) // Manually register the exception handler
                .build();
    }

    @Test
    @DisplayName("✅ signup() should create user and return 201 Created with token")
    void signup_WithValidData_ShouldReturnCreatedAndToken() throws Exception {
        // Arrange
        SignupRequestDto signupRequest = new SignupRequestDto("test@example.com", "password123", "Test User");
        UserDetails mockUserDetails = new User("test@example.com", "encoded-pass", Collections.emptyList());
        String mockToken = "mock.jwt.token";

        doNothing().when(authenticationService).createUser(anyString(), anyString(), anyString());
        when(authenticationService.authenticate(signupRequest.getEmail(), signupRequest.getPassword())).thenReturn(mockUserDetails);
        when(authenticationService.generateToken(mockUserDetails)).thenReturn(mockToken);

        // Act & Assert (This part remains exactly the same)
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(mockToken))
                .andExpect(jsonPath("$.expiresIn").value(86400));

        verify(authenticationService, times(1)).createUser("test@example.com", "password123", "Test User");
        verify(authenticationService, times(1)).authenticate("test@example.com", "password123");
        verify(authenticationService, times(1)).generateToken(mockUserDetails);
    }

    @Test
    @DisplayName("❌ signup() should return 400 Bad Request if email already exists")
    void signup_WhenEmailExists_ShouldReturnBadRequest() throws Exception {
        // Arrange
        SignupRequestDto signupRequest = new SignupRequestDto("existing@example.com", "password123", "Test User");

        doThrow(new IllegalArgumentException("Email is already in use."))
                .when(authenticationService).createUser(signupRequest.getEmail(), signupRequest.getPassword(), signupRequest.getName());

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email is already in use."));

        verify(authenticationService, never()).authenticate(any(), any());
        verify(authenticationService, never()).generateToken(any());
    }

    @Test
    @DisplayName("✅ login() should return 200 OK with token for valid credentials")
    void login_WithValidCredentials_ShouldReturnOkAndToken() throws Exception {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "password123");
        UserDetails mockUserDetails = new User("test@example.com", "encoded-pass", Collections.emptyList());
        String mockToken = "mock.jwt.token";

        when(authenticationService.authenticate(loginRequest.getEmail(), loginRequest.getPassword())).thenReturn(mockUserDetails);
        when(authenticationService.generateToken(mockUserDetails)).thenReturn(mockToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken));
    }

    @Test
    @DisplayName("❌ login() should return 401 Unauthorized for invalid credentials")
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "wrong-password");

        when(authenticationService.authenticate(anyString(), anyString()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Incorrect username or password."));

        verify(authenticationService, never()).generateToken(any());
    }
}