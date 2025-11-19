package com.atlas.tourguide.user;

import java.util.UUID;

public interface UserService {
  User getUserById(UUID id);
}
