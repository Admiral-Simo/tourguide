package com.atlas.tourguide.auth;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.atlas.tourguide.user.User;
import com.atlas.tourguide.user.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${jwt.secret}")
  private String secretKey;

  private final Long jwtExpiryMs = 86400000L;

  @Override
  public UserDetails authenticate(String email, String password) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    return userDetailsService.loadUserByUsername(email);
  }

  @Override
  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return Jwts.builder().setClaims(claims).setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
  }

  private Key getSigningKey() {
    byte[] keyBytes = secretKey.getBytes();
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private String extractUsername(String token) {
    return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
        .getBody().getSubject();
  }

  @Override
  public UserDetails validateToken(String token) {
    String username = extractUsername(token);
    return userDetailsService.loadUserByUsername(username);
  }

  @Override
  public void createUser(String email, String password, String name) {
    // Check if email is already taken
    Optional<User> existingUser = userRepository.findByEmail(email);
    if (existingUser.isPresent()) {
      throw new IllegalArgumentException("Email is already in use.");
    }
    String hashedPassword = passwordEncoder.encode(password);
    User newUser = User.builder().email(email).password(hashedPassword).name(name).build();
    userRepository.save(newUser);
  }

}
