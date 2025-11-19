package com.atlas.tourguide.integration_test;

import com.atlas.tourguide.PostgresContainerInitializer;
import com.atlas.tourguide.auth.dtos.AuthResponseDto;
import com.atlas.tourguide.auth.dtos.LoginRequestDto;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest implements PostgresContainerInitializer {

  @BeforeAll
  static void start() {
    PostgresContainerInitializer.startContainer();
  }

  @Autowired
  protected TestRestTemplate restTemplate;

  protected HttpHeaders getAuthHeaders(String email, String password) {
    LoginRequestDto loginRequest = new LoginRequestDto(email, password);
    AuthResponseDto authResponse = restTemplate.postForObject("/api/v1/auth/login", loginRequest,
        AuthResponseDto.class);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(authResponse.getToken());
    return headers;
  }
}