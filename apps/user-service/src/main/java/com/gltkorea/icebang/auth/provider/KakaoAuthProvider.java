package com.gltkorea.icebang.auth.provider;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.gltkorea.icebang.auth.dto.external.AuthRequest;

public class KakaoAuthProvider implements OAuth2AuthProvider {
  @Override
  public Authentication authenticateWithCode(String code) throws AuthenticationException {
    return null;
  }

  @Override
  public boolean supportsProvider(String oauthProvider) {
    return false;
  }

  @Override
  public Authentication authenticate(AuthRequest request) throws AuthenticationException {
    return null;
  }

  @Override
  public boolean supports(AuthRequest request) {
    return false;
  }
}
