package com.gltkorea.icebang.domain.auth.service;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gltkorea.icebang.domain.auth.model.AuthCredential;
import com.gltkorea.icebang.mapper.AuthMapper;

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

    // roles가 "ROLE_USER,ROLE_ADMIN" 형태의 문자열이라면 List로 변환
    if (user.getRoles() != null && !user.getRoles().isEmpty()) {
      String rolesString = user.getRoles().get(0); // GROUP_CONCAT 결과는 첫 번째 요소에 있음
      user.setRoles(Arrays.asList(rolesString.split(",")));
    } else {
      user.setRoles(Collections.emptyList());
    }

    return user;
  }
}
