package com.gltkorea.icebang.domain.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.gltkorea.icebang.auth.service.AuthService;
import com.gltkorea.icebang.domain.user.model.UserAccountPrincipal;
import com.gltkorea.icebang.domain.user.model.Users;

public class SecurityAuthenticateAdapter implements UserAuthService {
  private AuthService authService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Users user = authService.loadUser(username);
    return UserAccountPrincipal.builder().build(); // @TODO users -> userdetail로
  }
}
