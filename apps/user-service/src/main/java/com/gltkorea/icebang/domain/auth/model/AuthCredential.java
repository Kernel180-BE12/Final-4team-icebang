package com.gltkorea.icebang.domain.auth.model;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthCredential implements UserDetails {

  private BigInteger id;
  private String email;
  private String password;
  private String status;

  // roles -> Spring Security authority로 변환
  private List<String> roles;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(SimpleGrantedAuthority::new) // "ROLE_USER", "ROLE_ADMIN" 이런 값
        .collect(Collectors.toList());
  }

  @Override
  public String getUsername() {
    return email; // 로그인 ID는 email
  }

  @Override
  public boolean isAccountNonExpired() {
    return true; // 필요 시 status 기반으로 변경 가능
  }

  @Override
  public boolean isAccountNonLocked() {
    return !"LOCKED".equalsIgnoreCase(status);
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return !"DISABLED".equalsIgnoreCase(status);
  }
}
