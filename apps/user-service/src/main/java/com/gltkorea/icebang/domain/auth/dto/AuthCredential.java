package com.gltkorea.icebang.domain.auth.dto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gltkorea.icebang.domain.auth.enums.Role;

/**
 * Spring Security 인증을 위한 사용자 정보 클래스
 * - UserDetails 인터페이스 구현
 * - 로그인 성공 후 SecurityContext에 저장됨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class AuthCredential implements UserDetails {

  // 기본 사용자 정보
  private Long userId;           // 사용자 고유 ID
  private String username;       // 사용자명
  private String email;          // 이메일 (실제 로그인 ID)
  private String password;       // 암호화된 비밀번호
  private String userStatus;     // 계정 상태 (ACTIVE, SUSPENDED 등)
  private String fullName;       // 전체 이름

  // 조직 관련 정보
  private Long deptId;           // 부서 ID
  private Long positionId;       // 직급 ID
  private String deptName;       // 부서명
  private String positionTitle;  // 직급명
  private String orgName;        // 조직명

  // 권한 관련 정보
  private List<Role> roles;      // 사용자가 가진 역할 목록
  private List<String> permissions; // 사용자가 가진 권한 목록

  /**
   * Spring Security에서 사용하는 권한 목록 반환
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }

    // Role enum을 GrantedAuthority로 변환
    return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.name()))
            .collect(Collectors.toList());
  }

  /**
   * 사용자 비밀번호 반환
   */
  @Override
  public String getPassword() {
    return this.password;
  }

  /**
   * 사용자명 반환 (로그인은 이메일로 하므로 이메일 반환)
   */
  @Override
  public String getUsername() {
    return this.email != null ? this.email : this.username;
  }

  /**
   * 계정이 잠기지 않았는지 확인 (SUSPENDED가 아니면 true)
   */
  @Override
  public boolean isAccountNonLocked() {
    return !"SUSPENDED".equals(userStatus);
  }

  /**
   * 계정이 활성화되었는지 확인 (ACTIVE인 경우만 true)
   */
  @Override
  public boolean isEnabled() {
    return "ACTIVE".equals(userStatus);
  }

  // 편의 메서드들

  /**
   * 특정 역할을 가지는지 확인
   */
  public boolean hasRole(Role role) {
    return roles != null && roles.contains(role);
  }

  /**
   * 특정 권한을 가지는지 확인
   */
  public boolean hasPermission(String permission) {
    return permissions != null && permissions.contains(permission);
  }

  /**
   * 최고 권한 레벨 반환
   */
  public int getHighestRoleLevel() {
    if (roles == null || roles.isEmpty()) {
      return 0;
    }
    return roles.stream()
            .mapToInt(Role::getLevel)
            .max()
            .orElse(0);
  }

  /**
   * 관리자 권한 여부 확인
   */
  public boolean isAdmin() {
    // @TODO:: check
    if (roles == null) return false;
    return roles.stream().anyMatch(Role::isAdmin);
  }

  /**
   * 화면 표시용 이름 반환
   */
  public String getDisplayName() {
    if (fullName != null && !fullName.trim().isEmpty()) {
      return fullName;
    }
    return username != null ? username : email;
  }
}