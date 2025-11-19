package com.atlas.tourguide.user;

import com.atlas.tourguide.shared.security.BlogUserDetails;
import com.atlas.tourguide.user.dtos.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
  @GetMapping("/me")
  public ResponseEntity<UserProfileDto> getCurrentUser(
      @AuthenticationPrincipal BlogUserDetails userDetails) {
    User currentUser = userDetails.getUser();
    UserProfileDto profile = new UserProfileDto(currentUser.getId(), currentUser.getEmail(),
        currentUser.getName(), currentUser.getCreatedAt());
    return ResponseEntity.ok(profile);
  }
}