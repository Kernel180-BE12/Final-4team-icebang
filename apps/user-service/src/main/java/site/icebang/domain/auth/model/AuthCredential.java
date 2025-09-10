package site.icebang.domain.auth.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
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

  // MyBatis GROUP_CONCAT 결과를 List<String>으로 변환하는 setter
  public void setRoles(String rolesString) {
    if (rolesString != null && !rolesString.trim().isEmpty()) {
      this.roles = Arrays.asList(rolesString.split(","));
    } else {
      this.roles = new ArrayList<>();
    }
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public List<String> getRoles() {
    return roles != null ? roles : new ArrayList<>();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim())) // ROLE_ prefix 추가 + 공백 제거
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
