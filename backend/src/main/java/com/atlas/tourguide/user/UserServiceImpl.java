package com.atlas.tourguide.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  @Override
  public User getUserById(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
  }
}
