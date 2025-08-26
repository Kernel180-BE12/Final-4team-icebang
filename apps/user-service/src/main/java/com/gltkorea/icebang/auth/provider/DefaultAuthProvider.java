package com.gltkorea.icebang.auth.provider;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.gltkorea.icebang.auth.dto.external.AuthRequest;

public class DefaultAuthProvider implements AuthProvider {
  @Override
  public Authentication authenticate(AuthRequest request) throws AuthenticationException {
    return null;
  }

  @Override
  public boolean supports(AuthRequest request) {
    return false;
  }
}
