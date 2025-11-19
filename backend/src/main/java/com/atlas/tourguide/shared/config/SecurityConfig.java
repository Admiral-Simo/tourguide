package com.atlas.tourguide.shared.config;

import com.atlas.tourguide.shared.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.atlas.tourguide.user.UserRepository;
import com.atlas.tourguide.shared.security.BlogUserDetailsService;
import com.atlas.tourguide.auth.AuthenticationService;

@Configuration
public class SecurityConfig {
  @Bean
  JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationService authenticationService) {
    return new JwtAuthenticationFilter(authenticationService);
  }

  @Bean
  UserDetailsService userDetailsService(UserRepository userRepositoryi) {
    return new BlogUserDetailsService(userRepositoryi);
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/signup")
        .permitAll().requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/v1/posts/drafts").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
        .requestMatchers(HttpMethod.GET, "/actuator/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, "/api/v1/tags/**").permitAll().anyRequest()
        .authenticated()).csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
