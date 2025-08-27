package com.gltkorea.icebang.auth.provider;

import org.springframework.security.core.Authentication;

import com.gltkorea.icebang.auth.dto.OAuth2RequestWrapper;

public interface OAuth2AuthProvider extends AuthProvider<OAuth2RequestWrapper> {
  Authentication authenticateWithCode(OAuth2RequestWrapper oauthContent);
}
