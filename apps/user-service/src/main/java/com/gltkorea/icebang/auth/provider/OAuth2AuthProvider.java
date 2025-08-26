package com.gltkorea.icebang.auth.provider;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface OAuth2AuthProvider extends AuthProvider {
  Authentication authenticateWithCode(String code) throws AuthenticationException;

  boolean supportsProvider(String oauthProvider);
}
