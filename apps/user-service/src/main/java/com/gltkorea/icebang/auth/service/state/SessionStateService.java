package com.gltkorea.icebang.auth.service.state;

import org.springframework.security.core.userdetails.UserDetails;

public final class SessionStateService implements AuthStateService {
  @Override
  public String create(UserDetails userDetails) {
    return "";
  }

  @Override
  public UserDetails validate(String identifier) {
    return null;
  }
}
