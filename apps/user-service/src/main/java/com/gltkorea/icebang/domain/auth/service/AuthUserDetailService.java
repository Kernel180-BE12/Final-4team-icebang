package com.gltkorea.icebang.domain.auth.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gltkorea.icebang.domain.auth.dto.AuthCredential;
import com.gltkorea.icebang.domain.auth.enums.Role;
import com.gltkorea.icebang.entity.Users;
import com.gltkorea.icebang.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자가 이메일/비밀번호로 로그인 시도 Spring Security가 이 서비스의 loadUserByUsername() 호출 DB에서 사용자 정보, 역할, 권한 조회
 * AuthCredential 객체로 변환하여 반환 Spring Security가 비밀번호 검증 후 인증 완료
 *
 * <p>다음 단계 옵션:
 *
 * <p>SecurityConfig 업데이트 (AuthenticationManager 설정) Permissions enum 완성 기본 데이터 입력 SQL 작성 Spring
 * Security에서 사용자 정보를 로드하는 서비스 - UserDetailsService 인터페이스 구현 - 로그인 시 이메일을 받아서 DB에서 사용자 정보 조회 - 조회한
 * 정보를 AuthCredential 객체로 변환하여 반환 - Spring Security가 자동으로 이 서비스를 호출함
 */
@Slf4j // 로그 사용을 위한 Lombok 애노테이션
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class AuthUserDetailService implements UserDetailsService {

  // MyBatis 매퍼를 통해 DB 접근
  private final UserMapper userMapper;

  /**
   * Spring Security가 사용자 인증 시 호출하는 메서드 - 사용자가 로그인 화면에서 입력한 이메일을 받음 - DB에서 해당 사용자의 모든 정보를 조회 -
   * AuthCredential 객체로 변환하여 반환
   *
   * @param username 실제로는 이메일 주소 (로그인 ID)
   * @return 사용자 정보가 담긴 AuthCredential 객체
   * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
   */
  @Override
  public AuthCredential loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("사용자 로그인 시도: {}", username);
    // @TODO
    // 1. userDetailResultMap 조정
    // 2. userMapper.findByEmailWithDetails 한 번 호출로 필요한 모든 데이터 가져옴
    // 3. AuthCredential 필드 고민- 조직,부서, 등등 필요한지!

    try {
      // 1. 이메일로 기본 사용자 정보 조회
      Users user = userMapper.findByEmail(username);

      // 2. 사용자가 존재하지 않으면 예외 발생
      if (user == null) {
        log.warn("존재하지 않는 사용자 로그인 시도: {}", username);
        throw new UsernameNotFoundException("User not found with email: " + username);
      }

      log.debug("사용자 기본 정보 조회 성공: userId={}, email={}", user.getUserId(), user.getUserEmail());

      // 3. 사용자 상세 정보 조회 (조직 정보 포함)
      Users userWithDetails = userMapper.findByEmailWithDetails(username);
      if (userWithDetails != null) {
        // 상세 정보가 있으면 기본 정보에 추가
        user.setDeptName(userWithDetails.getDeptName());
        user.setPositionTitle(userWithDetails.getPositionTitle());
        user.setOrgName(userWithDetails.getOrgName());
        log.debug(
            "사용자 상세 정보 조회 성공: 부서={}, 직급={}, 조직={}",
            user.getDeptName(),
            user.getPositionTitle(),
            user.getOrgName());
      }

      // 4. 사용자의 역할 목록 조회
      List<String> roleNames = userMapper.findRolesByUserId(user.getUserId());
      log.debug("사용자 역할 조회 결과: userId={}, roles={}", user.getUserId(), roleNames);

      // 5. 사용자의 권한 목록 조회 (역할을 통해 간접적으로)
      List<String> permissions = userMapper.findPermissionsByUserId(user.getUserId());
      log.debug("사용자 권한 조회 결과: userId={}, permissions={}", user.getUserId(), permissions);

      // 6. 역할 문자열을 Role enum으로 변환
      List<Role> roles = convertStringRolesToEnums(roleNames);
      log.debug("Role enum 변환 완료: {}", roles);

      // 7. AuthCredential 객체 생성 및 반환
      AuthCredential authCredential =
          AuthCredential.builder()
              .userId(user.getUserId())
              .username(user.getUserName())
              .email(user.getUserEmail())
              .password(user.getUserPassword()) // 암호화된 비밀번호
              .userStatus(user.getUserStatus())
              .fullName(user.getDisplayName()) // 화면 표시용 이름
              .deptId(user.getDeptId())
              .positionId(user.getPositionId())
              .deptName(user.getDeptName())
              .positionTitle(user.getPositionTitle())
              .orgName(user.getOrgName())
              .roles(roles) // 역할 목록
              .permissions(permissions) // 권한 목록
              .build();

      log.info(
          "사용자 로그인 정보 로드 완료: userId={}, email={}, roles={}",
          user.getUserId(),
          user.getUserEmail(),
          roles);

      return authCredential;

    } catch (UsernameNotFoundException e) {
      // 사용자 없음 예외는 그대로 재발생
      throw e;

    } catch (Exception e) {
      // 기타 예외는 로그 남기고 사용자 없음으로 처리
      log.error("사용자 정보 로드 중 예외 발생: email={}", username, e);
      throw new UsernameNotFoundException("Failed to load user: " + username, e);
    }
  }

  /**
   * 역할 문자열 목록을 Role enum 목록으로 변환 - DB에서 조회한 문자열 역할명을 Java enum으로 변환 - 잘못된 역할명이 있으면 로그를 남기고 건너뜀
   *
   * @param roleNames DB에서 조회한 역할명 문자열 목록
   * @return Role enum 목록
   */
  private List<Role> convertStringRolesToEnums(List<String> roleNames) {
    if (roleNames == null || roleNames.isEmpty()) {
      log.warn("사용자에게 할당된 역할이 없음");
      return List.of(); // 빈 목록 반환
    }

    return roleNames.stream()
        .map(this::convertStringToRoleEnum) // 각 문자열을 enum으로 변환
        .filter(role -> role != null) // null 제거 (변환 실패한 것들)
        .collect(Collectors.toList());
  }

  /**
   * 역할 문자열 하나를 Role enum으로 변환 - 대소문자 구분 없이 변환 시도 - 변환 실패 시 경고 로그를 남기고 null 반환
   *
   * @param roleName 변환할 역할명 문자열
   * @return Role enum 또는 null
   */
  private Role convertStringToRoleEnum(String roleName) {
    if (roleName == null || roleName.trim().isEmpty()) {
      return null;
    }

    try {
      // 대소문자 통일하여 enum 변환 시도
      return Role.valueOf(roleName.toUpperCase().trim());

    } catch (IllegalArgumentException e) {
      // 존재하지 않는 역할명인 경우
      log.warn("알 수 없는 역할명 발견: '{}'. 이 역할은 무시됩니다.", roleName);
      return null;
    }
  }

  /**
   * 사용자 ID로 사용자 정보 조회 (내부 사용용) - 다른 서비스에서 사용자 정보가 필요할 때 사용 - 로그인과는 별도로 사용자 정보만 필요한 경우
   *
   * @param userId 조회할 사용자 ID
   * @return 사용자 정보가 담긴 AuthCredential 객체
   * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
   */
  public AuthCredential loadUserByUserId(Long userId) throws UsernameNotFoundException {
    log.debug("사용자 ID로 정보 조회: {}", userId);

    Users user = userMapper.findById(userId);
    if (user == null) {
      throw new UsernameNotFoundException("User not found with ID: " + userId);
    }

    // 이메일 기반 조회 메서드 재사용
    return loadUserByUsername(user.getUserEmail());
  }

  /**
   * 사용자의 권한 레벨 확인 (내부 사용용) - 특정 작업 수행 전 권한 체크할 때 사용
   *
   * @param userId 확인할 사용자 ID
   * @param requiredLevel 필요한 최소 권한 레벨
   * @return 권한이 충분하면 true
   */
  public boolean hasRequiredPermissionLevel(Long userId, int requiredLevel) {
    try {
      AuthCredential user = loadUserByUserId(userId);
      return user.getHighestRoleLevel() >= requiredLevel;

    } catch (Exception e) {
      log.error("권한 레벨 확인 중 예외 발생: userId={}", userId, e);
      return false; // 예외 발생 시 권한 없음으로 처리
    }
  }
}
