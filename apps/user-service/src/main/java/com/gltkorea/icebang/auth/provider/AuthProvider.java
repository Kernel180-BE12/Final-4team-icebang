package com.gltkorea.icebang.auth.provider;

import org.springframework.security.core.Authentication;

import com.gltkorea.icebang.auth.dto.AuthRequestWrapper;

public interface AuthProvider<T extends AuthRequestWrapper> {
  boolean supports(T request);

  Authentication authenticate(T request);
}
