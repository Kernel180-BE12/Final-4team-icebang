package com.gltkorea.icebang.domain.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.gltkorea.icebang.domain.user.model.UserAccountPrincipal;

public class UserAuthServiceImpl implements UserAuthService {
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return UserAccountPrincipal.builder().build();
  }
}
