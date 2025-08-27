package com.gltkorea.icebang.auth.provider;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.gltkorea.icebang.auth.dto.DefaultRequestWrapper;

@Component("default")
public class DefaultProvider implements AuthProvider<DefaultRequestWrapper> {
  @Override
  public boolean supports(DefaultRequestWrapper request) {
    return false;
  }

  @Override
  public Authentication authenticate(DefaultRequestWrapper request) {
    return null;
  }
}
