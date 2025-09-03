package com.gltkorea.icebang.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.gltkorea.icebang.domain.auth.dto.AuthCredential;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final AuthenticationManager authenticationManager;

  public AuthCredential login(String email, String password) {
    Authentication auth =
        authenticationManager.authenticate( // 3
            new UsernamePasswordAuthenticationToken(email, password));
    // 7
    return (AuthCredential) auth.getPrincipal();
  }
}
