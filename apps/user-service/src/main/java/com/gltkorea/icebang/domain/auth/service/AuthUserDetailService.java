package com.gltkorea.icebang.domain.auth.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gltkorea.icebang.domain.auth.dto.AuthCredential;
import com.gltkorea.icebang.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUserDetailService implements UserDetailsService {
  private final UserMapper userMapper;

  @Override
  public AuthCredential loadUserByUsername(String username) throws UsernameNotFoundException {
    // 4. MyBatis로 DB에서 사용자+역할+권한 조회
    // 5-1. 사용자가 없으면 예외 발생 //throw new UsernameNotFoundException("User not found: " + email);

    // 5-2. 권한 리스트 생성
    // List<GrantedAuthority> authorities = createAuthorities(user);

    // 6. UserPrincipal 생성하여 반환
    throw new RuntimeException("Not implemented");
  }

  private List<GrantedAuthority> createAuthorities(Users user) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    return authorities;
  }
}
