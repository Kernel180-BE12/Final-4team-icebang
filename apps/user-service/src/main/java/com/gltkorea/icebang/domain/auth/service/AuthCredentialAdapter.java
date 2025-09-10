package com.gltkorea.icebang.domain.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gltkorea.icebang.domain.auth.mapper.AuthMapper;
import com.gltkorea.icebang.domain.auth.model.AuthCredential;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthCredentialAdapter implements UserDetailsService {
  private final AuthMapper authMapper;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    AuthCredential user = authMapper.findUserByEmail(email);

    if (user == null) {
      throw new UsernameNotFoundException("User not found with email: " + email);
    }

    return user;
  }
}
