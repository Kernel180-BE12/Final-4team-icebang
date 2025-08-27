package com.gltkorea.icebang.auth.service.state;

import org.springframework.security.core.userdetails.UserDetails;

public sealed interface AuthStateService permits SessionStateService, JwtTokenStateService {
  String create(UserDetails userDetails);

  UserDetails validate(String identifier);
}
